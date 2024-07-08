A feladatleírás a félévi projektre: (2024)
Bevezetés
Ebben a projektben egy többfelhasználós TODO lista megvalósítása a feladat. Ehhez különféle task-ok kezelését kell megoldani egy központi szerverhez kapcsolódó klienseken keresztül. A task-okat kell tudni kategorizálni, a felhasználóknak kezelni kell a jogkörét, csoportokhoz való tartozását.

Specifikáció:
Ez nem az implementálandó osztályok/paraméterek teljes listája, hanem a feladat részletes leírása! 

User: a szerveren engedélyezett felhasználók. 

Tárolni kell a bejelentkezési adatokat (username+password) 
Azt is, hogy milyen csoportokhoz van hozzáférése 
Felhasználót lehet regisztrálni, csak regisztrált felhasználó tud bejelentkezni 
Task: a tárolandó teendők.  

Az egyes task-oknak legyen címe, leírása, létrehozás dátuma, határideje, prioritása, állapota, szerzője, csoportja, és a csoportból hozzárendelhető személyek (szerkesztők, dolgozók). 
Tárolni kell még a task szerkesztési előzményeit: felhasználó + időpont. 
Task-ot tudni kell létrehozni, szerkeszteni, megosztani (másik csoporttal) és törölni. 
Megosztás esetén is csak 1 példány legyen a task-ból, de azt mindkét csoport felhasználói lássák. 
Csoport: felhasználók csoportja 

A csoportnak van neve, tagjai (megfelelő jogosultsági körökkel) 
A csoporthoz tagokat lehet hozzáadni, eltávolítani, és a jogosultsági szintjüket változtatni. 
Jogosultsági szint: csoportonként osztályozza a tagokat. 

Minimum elvárt szintek: Tulajdonos, adminisztátor, tag. 
Minimum elvárt Speciális szintek(task-onként kerül kiosztásra): szerkesztő, dolgozó 
Tag: teendőket hozhat létre, illetve az általa létrehozott teendőket szerkesztheti. 
Szerkesztő: az adott task-ot szerkesztheti (leszámítva a címet). 
Dolgozó: az adott task állapotát állíthatja át. 
Adminisztrátor: A csoport összes task-ját szerkesztheti, törölheti, megoszthatja 
Tulajdonos: A csoport összes task-ját szerkesztheti, törölheti, megoszthatja, a csoporton belüli jogosultságokat állíthatja, törölheti a csoportot. Csoportonként 1 tulajdonos lehet. 

Elvárások:
Szerver:
perzisztensen(fájlba mentve) tárolja a felhasználókat, és a task-okat, valamint a csoportokat 
intézi a felhasználók regisztrálását/bejelentkezési kérelmeit 
kérésre elküldi a felhasználó számára látható taskok (szűrt) listáját 
Kliens:
Server Connection (cím,port megadása)
Register/Login
Task-ok listájának megjelenítése:
1.) Lista szűrése (keresés) cím, szerző, csoport alapján. (ömlesztett nézet);
2.) Csak azon elemek megjelenítése melyhez az adott felhasználó hozzá van rendelve dolgozónak(és/vagy szerkesztőnek)
3.) Lista csoportosítása az előző szűrő funkció segítségével, hogy a külön csoportokat külön blokkban jelenítse meg. (csoportonkénti nézet, a csoportok lehetnek külön füleken, de akár egy nézetben is, valahogy elválasztva.) 
Új task létrehozása (/meglévő szerkesztése) 
Kiválasztott task részletes megjelenítése 
Új csoport létrehozása, meglévő szerkesztése (tagok/jogosultságok) 
GUI
Extra funkciók: 
Végső pontszámon javíthat, de nem helyettesíti az alap funkciók meglétét 
Jogosultsági szintek csoportonkénti kezelése – A csoport szerkesztő nézetben be lehet állítani az adott csoporthoz, hogy mely jogosultsági szinteken milyen funkciók elérhetők. Új saját jogosultsági szintet hozhat létre a csoport Tulajdonosa. 
Tulajdonosi jogkör átruházása: Tulajdonos felajánlja, másik felhasználó elfogadja. 
csoport, felhasználó, (dinamikusan hozzáadott jogkör) kiválasztáskor mind a keresésnél, mind a task létrehozásakor legördülős menüben keresés + kiválasztás  
