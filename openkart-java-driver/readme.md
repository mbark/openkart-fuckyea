# Open Kart

Open Kart är ett spel löst baserad på battle mode från Mario Kart. Varje lag kontrollerar en gokart, och försöker sönder varandras balonger med röda skal, den spelare som är sist kvar med balonger vinner. 

Tävlingen går ut på att programmera en bot som spelar detta spel så bra som möjligt.

## Reglerna i ett nötskal

Grunderna

1. Upp till fyra spelare tävlar på en gång, varje spelare börjar med en (go)kart med tre balonger
2. Vapenlådor dyker slumpmässigt upp på spelplanen, om en kart åker över en vapenlåda får den ett rött skal. En kart kan samla upp till 5 röda skal.
3. Kartsen kan skjuta röda skal på varandra, skalen är lätt målsökande men har en ganska kort räckvidd.
4. Blir en kart träffad förlorar den en balong, och utan balonger kvar är man ute ur spelet
5. Varannan gång (1:a, 3:e, 5:e, etc) en kart får in en träff, så får den en ny balong. (dvs. lagom aggresivt spel lönar sig)
6. Kartsen kan försöka skjuta ner inkommande röda skal för att skydda sig, detta kostar dock 2 röda skal. (och är ganska svårt)
7. Spelet fortsätter tills bara en spelare är kvar, eller det har gått två minuter.

## Ranking

Spelarna är under hela matchen rankade efter antalet balonger, där fler balonger är bättre. Rankningen är *stabil*, vilket innebär att om två spelare har lika många balonger så kommer de behålla sin inbördes ordning de hade tidigare. (I början av matchen är alla rankade efter id, som slumpas ut)

Detta innebär bland annat:

* Den spelare som åker ut först kommer att komma sist
* Den spelare som får in den första träffen kommer få en extra balong och ta ledningen
* Om två spelare är kvar i slutet vinner den med mest balonger

## Implementation

Spelet är implementerat som ett json-API, som presenterar spelets status och tar emot kommandon som json över HTTP. Boten ska kodas som en klient som pratar med API:et. Detta innebär att ni får koda i vilket språk ni vill.

För de som vill spara tid och slippa komma igång med API:et finns en färdig klient skriven i Java. Om ni inte har en stark preferens för något annat språk så är det smart att använda den. Även om ni använder någonting annat så kan den vara bra som en referens.

Varje lag ska få en API-nyckel som de använder för att prata med API:et. Denna används för att unikt identifiera ditt lag.

Viktigt att tänka på: Finalen kommer inte spelas i datorsalarna utan på Nymble. Därför är det viktigt att se till att någon med laptop kan köra er lösning. Vi kommer ha en laptop som kan köra Java på finalen.

## Läsa spelstatus

API:et har bara en metod, som tar emot både GET och PUT (alternativt POST) kommandon. 

Kod som bygger på java klienten kommer inte se någon json eller http utan kommer istället få ett Java-objekt med motsvarande data.

Ett GET anrop till `http://example.com/api/GameState?apiKey=exampleApiKey` kommer att ge något liknande

```
{
   "error": null,
   "inGame": true,
   "secondsLeft": 87,
   "yourKart": {
     "id": 1,
     "xPos": 41.8262,
     "yPos": 65.5448,
     "direction": 2.638,
     "xSpeed": 0,
     "ySpeed": 0,
     "baloons": 3,
     "shells": 3,
     "hits": 0,
     "shellCooldownTimeLeft": 0,
     "stunnedTimeLeft": 0,
     "invulnerableTimeLeft": 0,
     "order": {
       "moveX": 41.8262,
       "moveY": 65.5448,
     }
   },
   "enemyKarts": [
     ...
   ],
   "shells": [
     {
       "id": 47,
       "xPos": 39.7014,
       "yPos": 46.0599,
       "direction": 5.2323,
       "xSpeed": 14.9035,
       "ySpeed": -26.0363,
       "timeLeft": 0.2,
       "ownerId": 4,
       "targetId": 1
     },
     ...
   ],
   "itemBoxes": [
     {
       "id": 9,
       "xPos": 94,
       "yPos": 86
     },
     ...
   ]
}
```

Svaret är ganska maffigt, men många koncept återkommer, och klienter behöver sällan bry sig om all data. En enkel klient som bara använder den viktigaste informationen är att rekomendera till en början.

* **error** kommer vara förhoppningsvis null, med kan också vara en sträng med ett felmeddelande. Denna bör presenteras för laget om den finns, Java-klienten gör detta automatiskt (skriver ut till stderr).

* **inGame** är true om din bot spelar ett spel just nu. Java-klienten kommer inte köra din kod om inGame är false.

* **secondsLeft** är antalet sekunder kvar på nuvarande match.

* **yourKart** innehåller information om din kart. 
* **enemyKarts** innehåller motsvarande information för alla fiender som är kvar i spelet
    * **id** är ett unikt id för karten
    * **xPos** och **yPos** är positionen på spelplanen
    * **direction** är kartens riktning och går ifrån 0 till 2π. 0 är positivt på x-axeln, pi/2 är positivt på y-axeln, etc (tänk enhetscirkeln)
    * **xSpeed** och **ySpeed** är mest med för bekvämlighet och ger den nuvarande hastigheten i x-led och y-led. Om karten forsätter i samma riktning kommer den vara på position `(xPos + xSpeed, yPos + ySpeed)` om en sekund. (Beräknas med `xSpeed = 10 * cos(direction); ySpeed = 10 * sin(direction);`, eller `xSpeed = 0; ySpeed = 0;` om karten står stilla)
    * **baloons** är antalet balonger karten har kvar
    * **shells** är antalet skal som karten har att skjuta med
    * **hits** är antalet träffar din kart fått in på andra karts, är antalet ojämt kommer karten få en balong på nästa träff. En kart med många hits kan vara farligt.
    * **shellCooldownTimeLeft** sätts till 1.0 (sekund) varje gång karten skjuter ett skal. Måste räkna ner till 0 innan karten kan skjuta ett till skal.
    * **stunnedTimeLeft** sätts till 2.0 (sekunder) varje gång karten blir träffad. Måste räkna ner till 0 innan karten kan röra sig eller skjuta igen.
    * **invulnerableTimeLeft** sätts till 5.0 (sekunder) varje gång karten blir träffad. Måste räkna ner till 0 innan karten kan bli träffad igen. Detta ger 3 sekunder då karten kan skjuta men inte bli träffad, och därmed har en stor fördel.
    * **order** innehåller **moveX** och **moveY** som är punkten dit karten är på väg. **Du kan inte se order för någon annan kart än din egen**
* **shells** är de röda skal som finns på spelplanen.
    * **id**, **xPos**, **yPos**, **direction**, **xSpeed** och **ySpeed** fungerar på motsvarande sätt som för karts. Skalets hastighet är dock 30/sekund och inte 10/sekund
    * **timeLeft** är antalet sekunder som skalet har kvar innan det försvinner. Kommer sättas till 1 när skalet skjuts. Tillsammans med hastigheten ger detta en maximal räckvidd på 30.
    * **ownerId** är id för karten som skjöt skalet
    * **targetId** är id för karten (eller skalet) som skalet siktar på. Ett skal kan bara träffa det som den siktar på (om något annat är ivägen, kommer skalet åka igenom). Är targetId samma som yourKart.id så är skalet på väg mot dig.
* **itemBoxes** är vapenlådor. De har bara ett unikt id och en position.

## Registrera din bot

Innan ni kan vara med i spelet måste ni registera er bot på servern. För att registrera sig skickar klienten en PUT (eller POST) till `http://example.com/api/GameState`

```
{
  "apiKey": "exampleApiKey",
  "teamName": "exampleName"
}
```

Lagnamnet kan inte ändras när det väl är satt (om ni inte kommer och frågar snällt). Lagnamnet får vara mellan 1 och 15 tecken, och får innehåla bokstäver, siffror, mellanslag och understreck.

Även om klienten inte är med i en match så bör den pinga API:et minst var 10:e sekund. Detta är dels för att servern ska veta att klienten är redo att vara med i en match, och för att klienten ska kunna veta att ett spel har startat.

För att registrera sig eller pinga servern måste PUT eller POST användas. Svaret på PUT anropet kommer vara samma som svaret för GET, vilket innebär att botten inte behöver, och inte heller bör, göra GET-anrop. (GET-anrop är egentligen bara till för att testa själv, i webbläsare och liknande). 

## Kontrollera din kart

API:et för att kontrollera karten är ganska simpelt. Det finns två kommandon

1. Åk till position (x,y)
2. Skjut ett skal mot karten/skalet med id

Kommandona skickas in genom att utöka kommandot ovan. PUT (eller POST) till `http://example.com/api/GameState`

```
{
  "apiKey": "exampleApiKey",
  "teamName": "exampleName"
  "order": {
    "moveX": 89.0,
    "moveY": 49.0
  }
}
```

Kommer att svänga och köra karten mot den angivna punkten. Notera att x och y måste vara i intervallet 0.0-100.0 för att vara en giltig order.

```
{
  "apiKey": "exampleApiKey",
  "teamName": "exampleName"
  "order": {
    "fireAt": 2
  }
}
```

Kommer skjuta ett skal mot karten (eller skalet) med id 2. Du måste ha minst 1 skal för att skjuta mot en kart, och minst 2 skal för att kunna skjuta mot ett annat skal. Skalet kommer skjutas rakt fram eller rakt bak, beroende åt vilket håll målet är.

Det är dock inte alls säkert att du träffar, även om målet är nära. Karten bör helst vara riktad rakt mot (eller rakt motsatt) det du vill skjuta på. 

Move och fireAt ordrar exekveras oberoende av varandra, skickas en fireAt order in så kommer en tidigare move-order fortfarande gälla. Det går också att kombinera båda typerna av ordrar i samma anrop.

```
{
  "apiKey": "exampleApiKey",
  "teamName": "exampleName"
  "order": {
    "moveX": 89.0,
    "moveY": 49.0,
    "fireAt": 2
  }
}
```

## Regler

Egentligen ganska självklara saker, men vi nämner dem ändå.

### 1. Inget djävulskap

Undvik att förstöra för andra, förstöra servern eller försöka läsa av andras anrop (även om det är ganska lätt över http, men vi kör det för att det är enklast).

### 2. Varje bot för sig själv

Ni får gärna prata med varandra och hjälpa varandra om ni vill, men koda inte era bottar så att de sammarbetar eller utbyter information på något sätt. Varje lag för sig.

### 3. Helt autonom bot

Botten ska vara helt självstyrande. Ni får inte ge input under spelet gång som ändrar hur botten beter sig.

## Turnering i finalen

Finalen kommer spelas som en turnering där det är fyra spelare i varje match, de två som vinner går vidare till nästa match. De två spelare som vinner i "finalen" möts i en 1 on 1 showdown för att bestämma den sanna vinnaren.

I det första steget i turneringen är det troligtvis inte så att antalet spelare går jämt upp. I sådana fall så kan spelare som kommit på tredjeplats (och eventuellt också fjärdeplats) få spela en gång till.

## Övningsmatcher

Innan finalen kommer det vara möjligt att spela övningsmatcher mot andra spelare.

Det är upp till varje lag om de vill tävla med en bot som är vältestad mot andra spelare, eller om de vill använda en otestad men överraskande teknik i finalen. (troligtvis är det bra att testa sin bot lite i vilket fall)

## Detaljerad spelmekanik

En bra bot måste kunna förutspå hur de andra spelarna rör sig, och framförallt vad den kan träffa med ett skal. För att det ska bli lättare så kommer här lite mer detaljer om spelmekaniken. Konstanterna här återfinns också i GameConstants.java i Java-klienten.

Spelplanen är 100x100 enheter stor.

10 stycken vapenlådor är slumpmässigt utplacerade på spelplanen. När en plockas upp kommer en ny dyka upp på en slumpmässig position.

Karts står antingen helt stilla, eller färdas framåt i 10 enheter per sekund (de behöver inte accelerera). En kart kan svänga med π/2 radianer per sekund (90 grader per sekund), det betyder att det tar 2 sekunder att vända sig om.

Skal kan skjutas antingen rakt fram eller rakt bak (detta görs automatiskt). Skalen färdas framåt i 30 enheter per sekund, svänger mot målet med π/2 radianer per sekund, och försvinner 1 sekund efter de har skjutits (om de inte träffat sitt mål). 

Det betyder att skal har en räckvidd på 30 enheter, men om de träffar beror också på hur målet rör sig under tiden skalen färdas. En fiende som kör rakt mot dig kan du träffa om denna är 40 enheter bort, om fienden istället kör rakt bort från dig måste istället vara inom 20 enheter för att skalet ska träffa. (en defensiv spelare har ett visst övertag)

Skalets hastighet är stort i förhållande till hur mycket det kan svänga. Det betyder att dit mål bör vara hyffsat mycket rakt fram (eller rakt bak). Om målet är nära måste det nästan vara precis rakt fram för att skalet ska hinna svänga. Bäst träffchans finns om målet är lagom långt bort.

När det gäller att skjuta ner andra skal så kommer båda skalen att färdas i 30 enheter / sekund mot varandra, marginalen för att svänga här är väldigt liten. Vill du inte att dina skal ska kunna skjutas ner, så kan det vara bra att skjuta mot sidan på ditt mål.