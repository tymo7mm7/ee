---
id: qml-gradients
title: Gradienty: parameter‑shift i inne metody
summary: Dokładny gradient obwodu z dwóch ewaluacji, różniczkowanie adjoint, SPSA i koszt pomiarowy.
level: zaawansowany
lab: shift
---
### Reguła przesunięcia parametru
Dla bramki $U(\theta)=e^{-i\theta G/2}$, gdzie generator $G$ ma wartości własne $\pm1$ (np. $X$, $Y$, $Z$ — czyli rotacje $R_X$, $R_Y$, $R_Z$), funkcja $f(\theta)=\langle\psi|U^\dagger(\theta)MU(\theta)|\psi\rangle$ ma postać $A\cos\theta+B\sin\theta+C$ i
$$\frac{\partial f}{\partial\theta}=\frac{f\big(\theta+\frac{\pi}{2}\big)-f\big(\theta-\frac{\pi}{2}\big)}{2}.$$
To **dokładny** gradient (nie przybliżenie!), liczony na tym samym sprzęcie tym samym obwodem z przesuniętym kątem (Mitarai i in. 2018; Schuld i in. 2019).

**Szkic dowodu:** skoro $f(\theta)=A\cos\theta+B\sin\theta+C$, to $f(\theta\pm\frac{\pi}{2})=\mp A\sin\theta\pm B\cos\theta+C$. Różnica podzielona przez 2 daje $-A\sin\theta+B\cos\theta=f'(\theta)$.

### Uogólnienia
- Parametr w kilku bramkach albo kąt zależny od danych ($R_Y(wx)$): reguła łańcuchowa — sumujemy przyczynki, $\partial_w f=x\cdot\partial_{\text{kąt}}f$.
- Generatory o większej liczbie wartości własnych (np. bramki sterowane jak CRX) — **uogólnione reguły** z większą liczbą przesunięć (np. 4 ewaluacje dla CRX).
- Pochodne wyższych rzędów (Hesjan) — również przez przesunięcia.

### Porównanie metod
| Metoda | Koszt gradientu ($P$ param.) | Na sprzęcie? | Uwagi |
|---|---|---|---|
| parameter‑shift | $2P$ obwodów | tak | dokładny, nieobciążony |
| różnice skończone | $P$–$2P$ obwodów | tak | obciążony; przy szumie shotów wariancja $\sim1/(h^2S)$ |
| backprop (symulator) | kilka ewaluacji, dużo pamięci | nie | jak w PyTorch, tylko symulacja |
| adjoint | ok. 1–2 ewaluacji, mało pamięci | nie | wykorzystuje odwracalność obwodu |
| SPSA | **2** obwody niezależnie od $P$ | tak | losowy kierunek, odporny na szum |

### Dlaczego nie różnice skończone?
Estymata z $S$ shotów ma szum $\sim1/\sqrt S$. Iloraz $\frac{f(\theta+h)-f(\theta-h)}{2h}$ dzieli ten szum przez $h$ — dla $h=0.01$ to stukrotne wzmocnienie. Parameter‑shift używa przesunięcia $\pi/2$, więc szumu nie wzmacnia.

### Kwantowy gradient naturalny
Uwzględnia geometrię przestrzeni stanów: $\boldsymbol\theta\leftarrow\boldsymbol\theta-\eta\,g^{+}(\boldsymbol\theta)\nabla\mathcal L$, gdzie $g$ to **tensor metryczny Fubiniego–Study'ego** (Stokes i in. 2020). Potrzeba mniej kroków, ale estymacja $g$ kosztuje $O(P^2)$ obwodów (przybliżenia blokowo‑diagonalne są tańsze).

> [!exam] Typowe pytanie
> „Ile obwodów potrzeba do gradientu 100 parametrów?” — parameter‑shift: 200 (razy liczba próbek danych, razy shoty). SPSA: 2 (ale estymata jest zaszumiona).

@@@ fiszki
Q: Zapisz regułę parameter‑shift dla rotacji Pauliego.
A: $\partial_\theta f=\frac12\big[f(\theta+\frac{\pi}{2})-f(\theta-\frac{\pi}{2})\big]$ dla $U(\theta)=e^{-i\theta G/2}$, gdzie $G$ ma wartości własne $\pm1$.

Q: Czy parameter‑shift jest przybliżeniem?
A: Nie — daje dokładny (nieobciążony) gradient; błąd pochodzi tylko ze skończonej liczby shotów.

Q: Dlaczego $f(\theta)$ dla rotacji Pauliego jest sinusoidą?
A: Bo $e^{-i\theta G/2}=\cos\frac{\theta}{2}I-i\sin\frac{\theta}{2}G$, więc $f(\theta)=A\cos\theta+B\sin\theta+C$.

Q: Czym jest metoda adjoint differentiation?
A: Metodą dla symulatorów, która dzięki odwracalności obwodu liczy wszystkie pochodne kosztem ok. jednego przebiegu w przód i w tył, przy małym zużyciu pamięci.

Q: Ile obwodów potrzebuje SPSA na jedną estymatę gradientu?
A: 2, niezależnie od liczby parametrów (zaburzenie wszystkich parametrów naraz w losowym kierunku).

Q: Dlaczego różnice skończone źle działają z szumem shotów?
A: Szum estymaty ($\sim1/\sqrt S$) jest dzielony przez mały krok $h$, więc wariancja gradientu rośnie jak $1/(h^2S)$.

Q: Co uwzględnia kwantowy gradient naturalny?
A: Geometrię przestrzeni stanów przez tensor metryczny Fubiniego–Study'ego: $\Delta\boldsymbol\theta=-\eta g^{+}\nabla\mathcal L$.

@@@ quiz
? $f(\theta)=\cos\theta$. Ile daje parameter‑shift w $\theta=0$?
+ 0
- 1
- −1
- 1/2
! $[\cos(\pi/2)-\cos(-\pi/2)]/2=0=-\sin 0$.

? Dla jakich bramek działa podstawowa (dwupunktowa) reguła parameter‑shift?
+ $e^{-i\theta G/2}$, gdzie $G$ ma wartości własne $\pm1$ (np. $R_X$, $R_Y$, $R_Z$)
- dla dowolnych bramek, także nieunitarnych
- tylko dla bramek CNOT
- tylko dla pomiarów
! Dla generatorów z większą liczbą wartości własnych potrzeba uogólnionych reguł.

? Ile obwodów wymaga parameter‑shift dla gradientu 50 parametrów (jedna próbka danych)?
- 50
+ 100
- 2
- 2500
! $2P=100$.

? Która metoda NIE jest dostępna na prawdziwym sprzęcie kwantowym?
- parameter‑shift
- SPSA
+ adjoint differentiation
- różnice skończone
! Adjoint wymaga dostępu do wektora stanu — tylko w symulatorach.

? Parametr $w$ występuje w bramce $R_Y(w\cdot x)$, gdzie $x=2$. Pochodna względem kąta wynosi $g$. Ile wynosi $\partial f/\partial w$?
- $g$
+ $2g$
- $g/2$
- $g+2$
! Reguła łańcuchowa: $\partial f/\partial w=x\cdot g$.
%%%
---
id: qml-reupload
title: Data re‑uploading i modele jako szeregi Fouriera
summary: Dlaczego warto kodować dane wielokrotnie i co wyznacza spektrum częstotliwości modelu.
level: zaawansowany
lab: train
---
### Data re‑uploading
Pérez‑Salinas i in. (*Quantum* 2020): naprzemienne warstwy kodowania i trenowalnych bramek
$$U(\mathbf x,\boldsymbol\theta)=W_L(\boldsymbol\theta_L)S(\mathbf x)\cdots W_1(\boldsymbol\theta_1)S(\mathbf x)$$
dają **uniwersalny klasyfikator** nawet na **jednym kubicie** — podobnie jak sieć z jedną warstwą ukrytą aproksymuje dowolną funkcję. Kodowanie danych tylko raz, na początku, silnie ogranicza model.

### Modele kwantowe to częściowe szeregi Fouriera
Schuld, Sweke i Meyer (*PRA* 2021): jeśli dane są kodowane jako $e^{-ixH}$ (np. $R_X(x)$, gdzie $H=X/2$ ma wartości własne $\pm\frac12$), to
$$f(x)=\sum_{\omega\in\Omega}c_\omega e^{i\omega x},$$
gdzie **spektrum** $\Omega$ to zbiór różnic sum wartości własnych generatorów kodujących.
- jedna rotacja Pauliego: $\Omega=\{-1,0,1\}$ — model to $a+b\cos x+c\sin x$,
- $L$ powtórzeń kodowania (szeregowo albo równolegle na $L$ kubitach): $\Omega=\{-L,\dots,L\}$,
- **współczynniki** $c_\omega$ zależą od trenowalnych bloków $W$, a **częstotliwości** — tylko od kodowania.

### Konsekwencje praktyczne
- Mało powtórzeń kodowania → model wyrazi tylko „wolne” funkcje (niskie częstotliwości) — niezależnie od liczby parametrów!
- Przeskalowanie wejścia $x\to wx$ (trenowalne $w$) zmienia częstotliwości — w Lab model VQC ma trenowalne wagi przy danych.
- Dla wielu cech mamy wielowymiarowy szereg Fouriera; spektrum rośnie z liczbą kodowań każdej cechy.
- Modele kwantowe mają **skłonność do niskich częstotliwości**, podobnie jak sieci neuronowe (*spectral bias*).

> [!ex] Przykład z Lab
> Na danych „Okrąg” VQC z $L=1$ ma ograniczoną klasę granic decyzyjnych, a przy $L=3$ uczy się okręgu prawie idealnie. Sprawdź!

> [!info] Dekwantyzacja przez Fouriera?
> Skoro model jest szeregiem Fouriera, można próbować aproksymować go klasycznie (np. *random Fourier features*, Landman i in. 2022). Dla niektórych architektur to działa, co podważa przewagę kwantową modeli z prostym kodowaniem.

@@@ fiszki
Q: Na czym polega data re‑uploading?
A: Na wielokrotnym kodowaniu danych przeplatanym z trenowalnymi blokami: $W_LS(\mathbf x)\cdots W_1S(\mathbf x)$.

Q: Kto pokazał, że jeden kubit z re‑uploadingiem jest uniwersalnym klasyfikatorem?
A: Pérez‑Salinas i in. (*Quantum* 2020), „Data re‑uploading for a universal quantum classifier”.

Q: Jaką postać ma model kwantowy z kodowaniem $e^{-ixH}$?
A: Częściowego szeregu Fouriera $\sum_{\omega\in\Omega}c_\omega e^{i\omega x}$ (Schuld, Sweke, Meyer 2021).

Q: Co wyznacza spektrum częstotliwości modelu, a co jego współczynniki?
A: Częstotliwości — wartości własne generatorów kodowania (i liczba powtórzeń); współczynniki — trenowalne bloki obwodu i obserwabla.

Q: Jakie spektrum daje $L$ powtórzeń kodowania jedną rotacją Pauliego?
A: Częstotliwości całkowite $\{-L,\dots,0,\dots,L\}$.

Q: Dlaczego samo zwiększanie liczby parametrów nie zwiększa częstotliwości modelu?
A: Bo spektrum zależy wyłącznie od kodowania danych — parametry zmieniają tylko współczynniki $c_\omega$.

@@@ quiz
? Model z jednokrotnym kodowaniem $R_X(x)$ na jednym kubicie może wyrazić funkcję:
+ $a+b\cos x+c\sin x$
- dowolną funkcję okresową
- $\cos(3x)$
- wielomian stopnia 5
! Spektrum to $\{-1,0,1\}$.

? Jak zwiększyć maksymalną częstotliwość modelu kwantowego?
- Dodać więcej trenowalnych parametrów po kodowaniu
+ Powtórzyć kodowanie danych więcej razy (lub przeskalować wejście)
- Zwiększyć liczbę shotów
- Użyć innej obserwabli
! Częstotliwości zależą od kodowania.

? Co jest kwantowym odpowiednikiem twierdzenia o uniwersalnej aproksymacji dla jednego kubitu?
+ Uniwersalność klasyfikatora z data re‑uploading
- Twierdzenie o nieklonowaniu
- Twierdzenie Gottesmana–Knilla
- Ograniczenie Holevo
! Pérez‑Salinas i in. 2020.
%%%
---
id: qml-optim
title: Optymalizacja, shoty i szum w treningu
summary: Optymalizatory gradientowe i bezgradientowe, budżet pomiarów, lokalne minima i mitygacja błędów.
level: średni
---
### Optymalizatory
**Gradientowe** (z parameter‑shift albo symulatora):
- **SGD / Adam** — standard; Adam dobrze znosi szum i różną skalę gradientów,
- **QNG** (quantum natural gradient) — szybsza zbieżność, droższy krok,
- **Rotosolve** — wykorzystuje sinusoidalność kosztu: dla każdego parametru wyznacza dokładne minimum z 3 ewaluacji ($f=A\sin(\theta+B)+C$).

**Bezgradientowe:**
- **COBYLA**, **Nelder–Mead**, **Powell** — dobre przy małej liczbie parametrów, słabo skalują,
- **SPSA** — 2 ewaluacje na krok niezależnie od $P$; odporny na szum, popularny na sprzęcie IBM.

### Budżet shotów
Każda ewaluacja ma szum $\sim1/\sqrt S$. Strategie:
- mało shotów na początku, więcej przy zbieżności (adaptacyjnie, np. *iCANS*, *Rosalin*),
- mini‑batche danych — klasyczne SGD i tak jest zaszumione, więc szum shotów często nie przeszkadza,
- grupowanie komutujących obserwabli, by mierzyć je jednocześnie.

### Krajobraz straty
- **lokalne minima i pułapki** — dla małych, niedoparametryzowanych obwodów krajobraz bywa bardzo trudny (Anschuetz & Kiani 2022: „Quantum variational algorithms are swamped with traps”),
- **nadparametryzacja** pomaga: gdy parametrów jest więcej niż wymiar osiągalnej rozmaitości stanów, złe minima znikają,
- parametry kątowe żyją na torusie (okresowość) — warto o tym pamiętać przy inicjalizacji i regularyzacji.

### Szum sprzętowy w treningu
- szum **koherentny** (np. przekłamany kąt bramki) — trening potrafi go częściowo skompensować,
- szum **niekoherentny** (depolaryzacja) — spłaszcza krajobraz; gradienty maleją wykładniczo z głębokością,
- **mitygacja błędów**: zero‑noise extrapolation, probabilistic error cancellation, mitygacja błędów odczytu (macierz pomyłek), Pauli twirling. Każda z nich zwiększa liczbę potrzebnych pomiarów.

> [!tip] Praktyczny przepis
> Zacznij od symulatora bez szumu (adjoint/backprop + Adam), potem przejdź na symulator z modelem szumu i shotami (parameter‑shift lub SPSA), a na końcu na sprzęt — z mitygacją błędów odczytu.

@@@ fiszki
Q: Dlaczego SPSA jest popularny na sprzęcie kwantowym?
A: Wymaga tylko 2 ewaluacji kosztu na krok niezależnie od liczby parametrów i dobrze znosi szum.

Q: Na czym polega Rotosolve?
A: Wykorzystuje sinusoidalność kosztu względem każdego parametru: z 3 ewaluacji wyznacza dokładne minimum po jednym parametrze (optymalizacja po współrzędnych).

Q: Jak nadparametryzacja wpływa na krajobraz straty?
A: Gdy parametrów jest dostatecznie dużo, złe minima lokalne zanikają i trening łatwiej zbiega (Larocca i in. 2023).

Q: Wymień trzy techniki mitygacji błędów.
A: Zero‑noise extrapolation, probabilistic error cancellation, mitygacja błędów odczytu (odwracanie macierzy pomyłek); także Pauli twirling.

Q: Jak szum depolaryzujący wpływa na gradienty?
A: Spłaszcza krajobraz — gradienty maleją wykładniczo z głębokością obwodu (noise‑induced barren plateaus).

@@@ quiz
? Który optymalizator potrzebuje tylko 2 ewaluacji kosztu na krok, niezależnie od liczby parametrów?
- Adam z parameter‑shift
+ SPSA
- Nelder–Mead
- metoda Newtona
! Simultaneous Perturbation Stochastic Approximation.

? Ile ewaluacji wymaga Rotosolve do wyznaczenia optimum jednego parametru?
- 1
+ 3
- $2P$
- potrzebny jest Hesjan
! Sinusoida $A\sin(\theta+B)+C$ ma 3 niewiadome.

? Jaki jest typowy skutek mitygacji błędów?
+ Mniejsze obciążenie wyniku kosztem większej liczby pomiarów (wariancji)
- Całkowite usunięcie szumu bez dodatkowego kosztu
- Zmniejszenie liczby kubitów
- Zamiana szumu niekoherentnego na koherentny
! Mitygacja to nie korekcja błędów — płacimy próbkami.
%%%
---
id: qml-barren
title: Barren plateaus i trenowalność
summary: Dlaczego gradienty znikają wykładniczo, jakie są przyczyny i jak się bronić.
level: zaawansowany
---
### Zjawisko
McClean i in. (*Nature Communications* 2018): dla losowo zainicjalizowanych, dostatecznie głębokich PQC (tworzących 2‑design) wariancja gradientu maleje **wykładniczo** z liczbą kubitów:
$$\mathbb E[\partial_k\mathcal L]=0,\qquad \mathrm{Var}[\partial_k\mathcal L]\in O(b^{-n}),\quad b>1.$$
Krajobraz jest prawie płaski („jałowy płaskowyż”). Żeby z dużym prawdopodobieństwem wykryć kierunek spadku, potrzeba **wykładniczo wielu shotów** — trening staje się niewykonalny. Problem dotyczy też metod bezgradientowych: różnice wartości funkcji też są wykładniczo małe.

### Przyczyny
1. **Ekspresywność** — obwód bliski losowemu operatorowi unitarnemu (2‑design).
2. **Globalna funkcja kosztu** — np. $1-p(0\dots0)$ na wszystkich kubitach. Cerezo i in. (2021): koszty globalne dają BP nawet dla płytkich obwodów; koszty lokalne (np. $\frac1n\sum_j\langle Z_j\rangle$) przy głębokości $O(\log n)$ dają gradienty tylko wielomianowo małe.
3. **Splątanie** — nadmiar splątania między kubitami widocznymi i ukrytymi (Ortiz Marrero i in. 2021).
4. **Szum** — depolaryzacja daje BP wykładnicze w głębokości (Wang i in. 2021).
5. **Kodowanie danych** — zbyt ekspresywne kodowanie lub koncentracja stanów danych.

### Strategie obrony
- **lokalne funkcje kosztu** zamiast globalnych,
- **płytkie, dopasowane do problemu** ansatze (symetrie, ekwiwariancja, QCNN),
- **inicjalizacja**: bloki identyczności, małe kąty, *warm start* z rozwiązania klasycznego,
- **trening warstwa po warstwie** (*layerwise learning*),
- korelowanie parametrów i ograniczanie osiągalnej przestrzeni stanów.

### BP a symulowalność — ważny wynik
Cerezo i in. (2023, „Does provable absence of barren plateaus imply classical simulability?”): wiele architektur z **dowiedzioną** ochroną przed BP działa w małej podprzestrzeni o wielomianowym wymiarze — a wtedy da się je **efektywnie symulować klasycznie** (czasem po etapie zbierania danych z urządzenia kwantowego). Wniosek: trenowalność i przewaga kwantowa mogą stać ze sobą w napięciu; potrzebne są nowe pomysły (np. ciepłe starty, wykorzystanie struktury problemu).

> [!exam] Pytania egzaminacyjne
> - Jak skaluje się wariancja gradientu w BP? (maleje wykładniczo z $n$)
> - Lokalny czy globalny koszt? (lokalny chroni przy płytkich obwodach)
> - Czy optymalizatory bezgradientowe pomagają? (nie)

@@@ fiszki
Q: Czym są barren plateaus?
A: Obszarami krajobrazu straty, w których wariancja gradientu maleje wykładniczo z liczbą kubitów — trening wymaga wykładniczo wielu pomiarów.

Q: Kto opisał barren plateaus i kiedy?
A: McClean, Boixo, Smelyanskiy, Babbush i Neven — *Nature Communications* 2018.

Q: Wymień główne przyczyny barren plateaus.
A: Duża ekspresywność (2‑design), globalne funkcje kosztu, nadmiar splątania, szum sprzętowy, zbyt ekspresywne kodowanie danych.

Q: Która funkcja kosztu jest bezpieczniejsza: lokalna czy globalna?
A: Lokalna (np. średnia $\langle Z_j\rangle$ po kubitach) — przy płytkich obwodach gradienty maleją co najwyżej wielomianowo (Cerezo i in. 2021).

Q: Czy optymalizatory bezgradientowe rozwiązują problem barren plateaus?
A: Nie — różnice wartości kosztu też są wykładniczo małe, więc nadal potrzeba wykładniczo wielu shotów.

Q: Jaki związek ma brak barren plateaus z symulowalnością klasyczną?
A: Cerezo i in. (2023): wiele modeli z dowiedzionym brakiem BP działa w małej podprzestrzeni i da się je efektywnie symulować klasycznie.

Q: Podaj trzy strategie łagodzenia barren plateaus.
A: Lokalne funkcje kosztu, inicjalizacja blokami identyczności lub małymi kątami, trening warstwa po warstwie, ansatze dopasowane do symetrii problemu.

@@@ quiz
? Jak skaluje się wariancja gradientu w reżimie barren plateau?
- Rośnie liniowo z liczbą kubitów
- Jest stała
+ Maleje wykładniczo z liczbą kubitów
- Maleje jak $1/n$
! $\mathrm{Var}[\partial\mathcal L]\in O(b^{-n})$.

? Która funkcja kosztu jest „globalna”?
+ $1-p(00\dots0)$ — prawdopodobieństwo, że wszystkie kubity są w stanie 0
- $\frac1n\sum_j\langle Z_j\rangle$
- $\langle Z_0\rangle$
- $\langle Z_0Z_1\rangle$
! Koszty globalne zależą od wszystkich kubitów jednocześnie.

? Szum depolaryzujący prowadzi do barren plateaus wykładniczych w:
- liczbie shotów
+ głębokości obwodu
- liczbie próbek danych
- liczbie epok
! Noise‑induced barren plateaus (Wang i in. 2021).

? Która strategia NIE pomaga na barren plateaus?
- Lokalna funkcja kosztu
- Inicjalizacja blokami identyczności
- Trening warstwa po warstwie
+ Zamiana Adama na optymalizator bezgradientowy przy tym samym obwodzie
! Metody bezgradientowe też muszą rozróżnić wykładniczo małe różnice.

? Co sugerują wyniki Cerezo i in. o „dowiedlnie trenowalnych” modelach?
+ Często da się je efektywnie symulować klasycznie
- Zawsze dają wykładniczą przewagę
- Wymagają korekcji błędów
- Nie da się ich zaimplementować
! To napięcie między trenowalnością a przewagą kwantową.
%%%
---
id: qml-arch
title: Architektury: QCNN, QGAN, Born machines, reservoir
summary: Przegląd najważniejszych modeli QML poza prostym VQC.
level: zaawansowany
---
### Kwantowa sieć konwolucyjna (QCNN)
Cong, Choi i Lukin (*Nature Physics* 2019). Warstwy:
- **konwolucja**: te same dwukubitowe operacje unitarne (współdzielone parametry) na sąsiednich parach,
- **pooling**: pomiar części kubitów i warunkowe bramki na pozostałych (albo ich odrzucenie) — liczba kubitów maleje o połowę,
- **warstwa pełna** na kilku ostatnich kubitach + pomiar.

Tylko $O(\log n)$ warstw i $O(\log n)$ parametrów. Pesah i in. (2021) wykazali, że QCNN **nie mają barren plateaus**. Zastosowania: rozpoznawanie faz kwantowych (dane kwantowe!), korekcja błędów. Uwaga: nowsze prace (np. Bermejo i in. 2024) pokazują, że wiele zadań QCNN da się efektywnie symulować klasycznie.

### Kwantowe sieci GAN (QGAN)
Generator to obwód kwantowy próbkujący bitstringi; dyskryminator — klasyczny lub kwantowy. Zastosowanie: ładowanie rozkładów prawdopodobieństwa, np. do wyceny opcji (Zoufal i in. 2019).

### Quantum Circuit Born Machine (QCBM)
Model generatywny, w którym rozkład próbek wynika z reguły Borna: $p_{\boldsymbol\theta}(x)=|\langle x|\psi(\boldsymbol\theta)\rangle|^2$. Trenuje się go np. funkcją MMD (*maximum mean discrepancy*). Próbkowanie jest „naturalne” dla komputera kwantowego — tu szuka się przewagi w modelowaniu rozkładów.

### Kwantowy autoenkoder
Romero i in. (2017): obwód kompresuje stan $n$‑kubitowy do $k<n$ kubitów; „śmieciowe” kubity są trenowane do stanu $|0\rangle$. Zastosowania: kompresja danych kwantowych, odszumianie.

### Quantum reservoir computing
Nietrenowany, złożony układ kwantowy (rezerwuar) przekształca dane, a trenuje się tylko **liniowy** odczyt z wielu obserwabli. Brak problemów z gradientami obwodu; naturalne dla platform analogowych (np. atomy Rydberga).

### Inne kierunki
- **kwantowe maszyny Boltzmanna** (próbkowanie z rozkładu Gibbsa),
- **kwantowe uczenie ze wzmocnieniem** (polityka jako PQC),
- hybrydowy **transfer learning**: pretrenowana sieć klasyczna + kwantowa „głowa”,
- **sieci tensorowe** (MPS, TTN) — klasyczne, inspirowane kwantowo; często mocna klasyczna baza dla QML.

@@@ fiszki
Q: Z jakich warstw składa się QCNN?
A: Z warstw konwolucyjnych (współdzielone dwukubitowe operacje), poolingu (pomiar lub odrzucenie połowy kubitów + bramki warunkowe) i końcowej warstwy pełnej.

Q: Ile parametrów ma QCNN dla $n$ kubitów?
A: $O(\log n)$ — tyle co warstw, dzięki współdzieleniu parametrów.

Q: Czy QCNN mają barren plateaus?
A: Nie — Pesah i in. (2021) wykazali, że QCNN nie mają barren plateaus.

Q: Czym jest Quantum Circuit Born Machine?
A: Modelem generatywnym, w którym rozkład próbek to $p(x)=|\langle x|\psi(\boldsymbol\theta)\rangle|^2$; trenowany np. funkcją MMD.

Q: Na czym polega quantum reservoir computing?
A: Nietrenowany układ kwantowy przetwarza dane, a trenuje się tylko liniowy odczyt z obserwabli.

Q: Do czego służy kwantowy autoenkoder?
A: Do kompresji stanów kwantowych do mniejszej liczby kubitów (kubity „śmieciowe” trenowane do $|0\rangle$).

@@@ quiz
? Która architektura powstała do rozpoznawania faz kwantowych i ma $O(\log n)$ parametrów?
+ QCNN
- QGAN
- QCBM
- Quantum reservoir
! Cong, Choi i Lukin 2019.

? Co trenuje się w quantum reservoir computing?
- Wszystkie bramki rezerwuaru
+ Tylko liniową warstwę odczytu
- Tylko kodowanie danych
- Nic — model nie jest trenowany
! Rezerwuar jest stały, co eliminuje problem gradientów obwodu.

? Z czego wynika rozkład modelu QCBM?
- Z funkcji softmax
+ Z reguły Borna: $|\langle x|\psi(\boldsymbol\theta)\rangle|^2$
- Z rozkładu Gibbsa
- Z twierdzenia Bayesa
! Born machine — próbki to wyniki pomiarów.
