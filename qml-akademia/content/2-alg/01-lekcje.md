---
id: alg-dj
title: Interferencja, wyrocznie i Deutsch–Jozsa
summary: Pierwszy algorytm z wykładniczą przewagą zapytań — dzięki phase kickback i interferencji.
level: średni
lab: circuit
---
### Model wyroczni
Funkcję $f:\{0,1\}^n\to\{0,1\}$ dostajemy jako czarną skrzynkę (wyrocznię) w postaci unitarnej:
$$U_f|x\rangle|y\rangle=|x\rangle|y\oplus f(x)\rangle.$$
Złożoność mierzymy **liczbą zapytań** do $U_f$.

### Phase kickback
Jeśli kubit pomocniczy jest w stanie $|-\rangle$, to
$$U_f|x\rangle|-\rangle=(-1)^{f(x)}|x\rangle|-\rangle.$$
Wartość funkcji „ląduje” w fazie rejestru wejściowego — to **wyrocznia fazowa** $|x\rangle\mapsto(-1)^{f(x)}|x\rangle$.

### Problem Deutscha–Jozsy
Obietnica: $f$ jest **stała** (wszędzie 0 albo wszędzie 1) albo **zrównoważona** (połowa zer, połowa jedynek). Trzeba rozstrzygnąć, który przypadek zachodzi.
- Klasycznie i deterministycznie: w najgorszym razie $2^{n-1}+1$ zapytań.
- Kwantowo: **1 zapytanie**.

**Algorytm:**
1. Stan $|0\rangle^{\otimes n}|1\rangle$; nałóż $H$ na wszystkie kubity.
2. Zastosuj $U_f$ (phase kickback): $\frac{1}{\sqrt{2^n}}\sum_x(-1)^{f(x)}|x\rangle$.
3. Nałóż $H^{\otimes n}$ na rejestr wejściowy i zmierz.

Amplituda $|0\dots0\rangle$ po kroku 3 wynosi $\frac{1}{2^n}\sum_x(-1)^{f(x)}$: równa się $\pm1$ dla $f$ stałej i $0$ dla zrównoważonej. Wynik „same zera” ⇔ $f$ stała.

### Lekcja: interferencja
Hadamardy rozkładają amplitudę na wszystkie ścieżki, wyrocznia zmienia znaki, drugie Hadamardy sprawiają, że ścieżki **interferują**: konstruktywnie wzmacniają poprawną odpowiedź, destruktywnie wygaszają złe. Ten schemat (superpozycja → faza → interferencja) powtarza się w algorytmach Bernsteina–Vaziraniego, Simona, Shora i Grovera.

> [!warn] Uczciwe porównanie
> Klasyczny algorytm **probabilistyczny** rozwiąże DJ z błędem $2^{-k}$ po $k+1$ zapytaniach — przewaga jest wykładnicza tylko względem algorytmów deterministycznych.

@@@ fiszki
Q: Jak działa wyrocznia $U_f$ w standardowym modelu?
A: $U_f|x\rangle|y\rangle=|x\rangle|y\oplus f(x)\rangle$.

Q: Na czym polega phase kickback?
A: Gdy cel jest w $|-\rangle$, $U_f$ daje $(-1)^{f(x)}|x\rangle|-\rangle$ — wartość $f$ trafia do fazy rejestru wejściowego.

Q: Jaki problem rozwiązuje algorytm Deutscha–Jozsy i ile zapytań potrzebuje?
A: Odróżnia funkcję stałą od zrównoważonej (przy obietnicy) jednym zapytaniem.

Q: Jaki wynik pomiaru w algorytmie DJ oznacza funkcję stałą?
A: Same zera $|0\dots0\rangle$ (z prawdopodobieństwem 1).

Q: Jaki ogólny schemat wykorzystują algorytmy kwantowe?
A: Superpozycja (H) → zakodowanie informacji w fazach (wyrocznia) → interferencja (H lub QFT) → pomiar.

@@@ quiz
? Ile zapytań do wyroczni potrzebuje algorytm Deutscha–Jozsy?
+ 1
- $n$
- $2^{n-1}+1$
- $\sqrt{2^n}$
! Jedno zapytanie dzięki superpozycji i interferencji.

? Dlaczego kubit pomocniczy przygotowuje się w stanie $|-\rangle$?
- Aby zwiększyć liczbę kubitów
+ Aby wartość $f(x)$ pojawiła się jako faza $(-1)^{f(x)}$ (phase kickback)
- Aby zmierzyć go w bazie X
- Bo $|-\rangle$ jest jedynym stanem stabilnym
! $|-\rangle$ to wektor własny $X$ z wartością własną $-1$.

? Funkcja jest zrównoważona. Jakie jest prawdopodobieństwo wyniku $|0\dots0\rangle$ w DJ?
- 1
- 1/2
+ 0
- $2^{-n}$
! Amplituda $\frac{1}{2^n}\sum_x(-1)^{f(x)}=0$ — destruktywna interferencja.

? Przewaga algorytmu DJ jest wykładnicza względem:
+ klasycznych algorytmów deterministycznych
- wszystkich algorytmów klasycznych, także probabilistycznych
- algorytmu Grovera
- obliczeń analogowych
! Algorytm losowy rozwiązuje DJ z małym błędem w kilku zapytaniach.
%%%
---
id: alg-bv
title: Bernstein–Vazirani i algorytm Simona
summary: Odczytanie ukrytego ciągu jednym zapytaniem i pierwsza wykładnicza przewaga nad algorytmami losowymi.
level: średni
---
### Bernstein–Vazirani
Funkcja $f(x)=s\cdot x \bmod 2$ (iloczyn skalarny bitów) z ukrytym ciągiem $s\in\{0,1\}^n$. Klasycznie potrzeba $n$ zapytań (np. $x=e_i$ odczytuje bit $s_i$). Kwantowo — **jedno**, tym samym obwodem co DJ. Po wyroczni fazowej stan to
$$\frac{1}{\sqrt{2^n}}\sum_x(-1)^{s\cdot x}|x\rangle=H^{\otimes n}|s\rangle,$$
więc kolejne $H^{\otimes n}$ daje dokładnie $|s\rangle$.

### Problem Simona
Obietnica: $f(x)=f(y)\iff y=x\oplus s$ dla ukrytego $s\neq0$ (funkcja 2‑do‑1 z „okresem” względem XOR). Znajdź $s$.
- Klasycznie (także probabilistycznie): $\Omega(2^{n/2})$ zapytań (paradoks urodzin).
- Kwantowo: $O(n)$ zapytań + klasyczna eliminacja Gaussa.

**Krok kwantowy:** $H^{\otimes n}$ → $U_f$ (wynik do drugiego rejestru) → pomiar drugiego rejestru (pierwszy przechodzi w $\frac{1}{\sqrt2}(|x_0\rangle+|x_0\oplus s\rangle)$) → $H^{\otimes n}$ → pomiar daje losowe $y$ spełniające $y\cdot s=0$. Po około $n$ powtórzeniach mamy układ równań liniowych nad $\mathbb{F}_2$ wyznaczający $s$.

> [!info] Znaczenie historyczne
> Simon (1994) pokazał pierwszą **wykładniczą** separację względem algorytmów probabilistycznych w modelu wyroczni. Zainspirował Shora: znajdowanie okresu funkcji to ten sam schemat, tylko nad grupą $\mathbb{Z}_N$ zamiast $\mathbb{Z}_2^n$ — z QFT zamiast Hadamardów.

> [!tip] Problem ukrytej podgrupy
> DJ, BV, Simon i Shor (faktoryzacja, logarytm dyskretny) to przypadki **problemu ukrytej podgrupy** (HSP) dla grup abelowych — rozwiązywalnego efektywnie przez QFT.

@@@ fiszki
Q: Jaki problem rozwiązuje algorytm Bernsteina–Vaziraniego?
A: Znajduje ukryty ciąg $s$ dla $f(x)=s\cdot x\bmod2$ jednym zapytaniem (klasycznie potrzeba $n$).

Q: Jaka jest obietnica w problemie Simona?
A: $f(x)=f(y)$ wtedy i tylko wtedy, gdy $y=x\oplus s$ dla ukrytego $s$.

Q: Ile zapytań wymaga problem Simona klasycznie, a ile kwantowo?
A: Klasycznie $\Omega(2^{n/2})$, kwantowo $O(n)$ (+ eliminacja Gaussa).

Q: Jaką informację o $s$ daje jedno uruchomienie obwodu Simona?
A: Losowy ciąg $y$ spełniający $y\cdot s=0 \pmod 2$.

Q: Czym jest problem ukrytej podgrupy (HSP)?
A: Ogólnym problemem obejmującym Simona, Shora i logarytm dyskretny; dla grup abelowych rozwiązywalnym efektywnie przez QFT.

@@@ quiz
? Ile zapytań kwantowych wymaga Bernstein–Vazirani dla $n$-bitowego $s$?
+ 1
- $n$
- $\log n$
- $2^n$
! Jeden obwód daje bezpośrednio $|s\rangle$.

? Co było przełomowe w algorytmie Simona?
- Był to pierwszy algorytm kwantowy w ogóle
+ Pierwsza wykładnicza przewaga nad algorytmami probabilistycznymi (w modelu wyroczni)
- Kwadratowe przyspieszenie przeszukiwania
- Pierwsza implementacja na sprzęcie
! Bezpośrednio zainspirował algorytm Shora.

? Jaka klasyczna procedura kończy algorytm Simona?
- Szybka transformata Fouriera
+ Eliminacja Gaussa nad $\mathbb{F}_2$
- Algorytm Euklidesa
- Ułamki łańcuchowe
! Zebrane $y$ z $y\cdot s=0$ tworzą układ równań liniowych modulo 2.
%%%
---
id: alg-qft
title: Kwantowa transformata Fouriera i estymacja fazy
summary: QFT w O(n²) bramkach oraz QPE — serce algorytmu Shora, HHL i symulacji chemii.
level: zaawansowany
lab: circuit
---
### QFT
Dla $N=2^n$:
$$\mathrm{QFT}\,|x\rangle=\frac{1}{\sqrt N}\sum_{k=0}^{N-1}e^{2\pi i\,xk/N}|k\rangle.$$
To dyskretna transformata Fouriera wektora amplitud. Kluczowa obserwacja — **postać iloczynowa** (zapis binarny $0.x_jx_{j+1}\dots$):
$$\mathrm{QFT}|x_1\dots x_n\rangle=\tfrac{1}{\sqrt{2^n}}\big(|0\rangle+e^{2\pi i\,0.x_n}|1\rangle\big)\otimes\big(|0\rangle+e^{2\pi i\,0.x_{n-1}x_n}|1\rangle\big)\otimes\cdots\otimes\big(|0\rangle+e^{2\pi i\,0.x_1\dots x_n}|1\rangle\big)$$
Obwód: na każdym kubicie $H$, potem sterowane fazy $R_k=P(2\pi/2^k)$ od kolejnych kubitów, na końcu odwrócenie kolejności (SWAP). Liczba bramek: $O(n^2)$ (przybliżona QFT: $O(n\log n)$). Klasyczna FFT na $2^n$ liczbach kosztuje $O(n2^n)$.

> [!warn] Haczyk
> QFT nie „przyspiesza FFT” w praktyce: nie da się odczytać $2^n$ współczynników — pomiar daje jeden wynik. QFT jest użyteczna, gdy szukamy **struktury** (okresu, fazy), którą da się odczytać z kilku próbek.

### Estymacja fazy (QPE)
Dane: unitarne $U$ i jego wektor własny $|u\rangle$, $U|u\rangle=e^{2\pi i\varphi}|u\rangle$. Cel: $\varphi$ z dokładnością $t$ bitów.
1. Rejestr $t$ kubitów w stanie $|+\rangle^{\otimes t}$.
2. Sterowane $U^{2^j}$ (phase kickback): $\frac{1}{\sqrt{2^t}}\sum_ke^{2\pi i\varphi k}|k\rangle|u\rangle$.
3. Odwrotna QFT → pomiar daje $\varphi$ z dokładnością $2^{-t}$ (z wysokim prawdopodobieństwem).

### Zastosowania QPE
- **Shor**: QPE dla $U|y\rangle=|ay\bmod N\rangle$ daje okres,
- **chemia kwantowa**: energia stanu podstawowego jako faza $e^{-iHt}$,
- **HHL**: odwracanie macierzy (QPE wyciąga wartości własne).

Koszt: głębokie obwody (sterowane $U^{2^j}$) — potrzebne maszyny odporne na błędy. Dlatego w erze NISQ wybiera się VQE.

> [!tip] Lab
> Preset „QFT na 3 kubitach” w symulatorze obwodów pokazuje, że po QFT wszystkie prawdopodobieństwa są równe, a informacja siedzi w fazach.

@@@ fiszki
Q: Zdefiniuj QFT na stanie bazowym $|x\rangle$.
A: $\mathrm{QFT}|x\rangle=\frac{1}{\sqrt N}\sum_{k}e^{2\pi ixk/N}|k\rangle$, gdzie $N=2^n$.

Q: Ile bramek wymaga QFT na $n$ kubitach?
A: $O(n^2)$ (Hadamardy, sterowane fazy i SWAP-y); wersja przybliżona — $O(n\log n)$.

Q: Dlaczego QFT nie zastępuje klasycznej FFT?
A: Bo nie da się odczytać wszystkich $2^n$ współczynników — pomiar daje jedną próbkę.

Q: Jaki problem rozwiązuje QPE?
A: Szacuje fazę $\varphi$ wartości własnej $e^{2\pi i\varphi}$ operatora unitarnego $U$ dla danego wektora własnego.

Q: Jakie są kroki QPE?
A: Hadamardy na rejestrze licznikowym → sterowane $U^{2^j}$ (phase kickback) → odwrotna QFT → pomiar.

@@@ quiz
? Jaka jest złożoność bramkowa dokładnej QFT na $n$ kubitach?
- $O(n)$
+ $O(n^2)$
- $O(n2^n)$
- $O(2^n)$
! $n$ Hadamardów i $n(n-1)/2$ sterowanych faz.

? Które zastosowanie NIE opiera się bezpośrednio na QPE?
- Algorytm Shora
- HHL
- Estymacja energii w chemii kwantowej
+ Algorytm Grovera
! Grover używa amplifikacji amplitudy (choć z QPE można zbudować kwantowe zliczanie).

? Co uzyskujemy z rejestru licznikowego QPE po odwrotnej QFT?
+ Przybliżenie fazy $\varphi$ z dokładnością $t$ bitów
- Wektor własny $|u\rangle$
- Wszystkie wartości własne $U$ naraz
- Macierz $U$
! Pomiar daje binarne rozwinięcie $\varphi$.
%%%
---
id: alg-shor
title: Algorytm Shora
summary: Faktoryzacja w czasie wielomianowym przez znajdowanie okresu — i co to oznacza dla RSA.
level: zaawansowany
---
### Od faktoryzacji do okresu
Aby rozłożyć $N$ na czynniki:
1. Wylosuj $a<N$; jeśli $\gcd(a,N)>1$ — mamy dzielnik.
2. Znajdź **rząd** $r$: najmniejsze $r$ z $a^r\equiv1\pmod N$ (krok kwantowy).
3. Jeśli $r$ jest parzyste i $a^{r/2}\not\equiv-1\pmod N$, to $\gcd(a^{r/2}\pm1,N)$ daje nietrywialne dzielniki (bo $N\mid(a^{r/2}-1)(a^{r/2}+1)$).

Krok 3 udaje się z prawdopodobieństwem co najmniej 1/2 dla losowego $a$.

### Część kwantowa
Funkcja $f(x)=a^x\bmod N$ jest okresowa z okresem $r$. Obwód:
- superpozycja $\sum_x|x\rangle$, obliczenie $|x\rangle|a^x\bmod N\rangle$ (**potęgowanie modularne** — najdroższa część),
- odwrotna QFT na pierwszym rejestrze → pomiar daje w przybliżeniu $k\cdot2^t/r$,
- **ułamki łańcuchowe** wyciągają $r$.

Równoważnie: QPE dla $U_a|y\rangle=|ay\bmod N\rangle$, którego wartości własne to $e^{2\pi ik/r}$.

### Złożoność
Kwantowo: $O(n^3)$ (lub lepiej) dla $n=\log_2 N$ bitów. Najlepszy znany algorytm klasyczny (sito ciała liczbowego): $\exp\big(O(n^{1/3}\log^{2/3}n)\big)$ — przewaga **superwielomianowa**.

### Konsekwencje
- Łamie RSA, Diffiego–Hellmana i kryptografię na krzywych eliptycznych (wariant dla logarytmu dyskretnego).
- Szacunki (Gidney 2025): RSA‑2048 wymagałby poniżej miliona zaszumionych kubitów fizycznych i około tygodnia obliczeń — daleko poza obecnym sprzętem, ale to motywuje **kryptografię postkwantową** (standardy NIST 2024: ML‑KEM, ML‑DSA, SLH‑DSA).
- Strategia „harvest now, decrypt later” — dane szyfrowane dziś mogą zostać odczytane w przyszłości.

> [!exam] Do zapamiętania
> Shor = redukcja klasyczna (faktoryzacja → rząd) + kwantowe znajdowanie okresu (QFT/QPE) + ułamki łańcuchowe.

@@@ fiszki
Q: Do jakiego problemu Shor redukuje faktoryzację?
A: Do znalezienia rzędu $r$ liczby $a$ modulo $N$ ($a^r\equiv1\bmod N$), czyli okresu funkcji $a^x\bmod N$.

Q: Jak z rzędu $r$ otrzymać dzielnik $N$?
A: Jeśli $r$ jest parzyste i $a^{r/2}\not\equiv-1$, to $\gcd(a^{r/2}\pm1,N)$ to nietrywialne dzielniki.

Q: Która część algorytmu Shora jest najdroższa?
A: Potęgowanie modularne $|x\rangle|0\rangle\mapsto|x\rangle|a^x\bmod N\rangle$.

Q: Jaką metodą klasyczną kończy się algorytm Shora?
A: Rozwinięciem w ułamek łańcuchowy, by z wyniku $\approx k/r$ odczytać $r$.

Q: Jakie systemy kryptograficzne łamie algorytm Shora?
A: RSA, Diffiego–Hellmana i kryptografię na krzywych eliptycznych (logarytm dyskretny).

Q: Czym jest kryptografia postkwantowa?
A: Klasycznymi algorytmami odpornymi na znane ataki kwantowe, np. opartymi na kratach (ML‑KEM, ML‑DSA — standardy NIST 2024).

@@@ quiz
? Jaki jest kluczowy krok kwantowy algorytmu Shora?
- Przeszukiwanie Grovera
+ Znajdowanie okresu funkcji $a^x\bmod N$
- Obliczenie $\gcd$
- Test pierwszości
! Okres = rząd $a$ modulo $N$, wyznaczany przez QFT/QPE.

? Dla $N=15$, $a=7$ rząd wynosi $r=4$ (bo $7^4=2401\equiv1$). Jakie dzielniki daje $\gcd(7^2\pm1,15)$?
+ 3 i 5
- 1 i 15
- 7 i 2
- 4 i 6
! $7^2=49$; $\gcd(48,15)=3$, $\gcd(50,15)=5$.

? Jaki charakter ma przewaga Shora nad najlepszymi znanymi algorytmami klasycznymi?
- Kwadratowy
+ Superwielomianowy (praktycznie wykładniczy)
- Liniowy
- Brak przewagi
! Klasycznie sito ciała liczbowego jest subwykładnicze, kwantowo — wielomianowo.

? Dlaczego RSA‑2048 nie jest dziś łamany kwantowo?
- Bo algorytm Shora działa tylko dla liczb pierwszych
+ Bo potrzeba bardzo dużej, odpornej na błędy maszyny (rzędu miliona kubitów fizycznych)
- Bo RSA używa krzywych eliptycznych
- Bo QFT nie da się zaimplementować
! Obecne urządzenia są za małe i zbyt zaszumione.
%%%
---
id: alg-grover
title: Algorytm Grovera i amplifikacja amplitudy
summary: Przeszukiwanie nieuporządkowanej bazy w O(√N) — kwadratowe przyspieszenie.
level: średni
lab: circuit
---
### Problem
Spośród $N=2^n$ elementów szukamy $x^*$ z $f(x^*)=1$ (wyrocznia rozpoznaje rozwiązanie). Klasycznie potrzeba $O(N)$ zapytań. Grover: $O(\sqrt N)$ — i to jest **optymalne** (dolna granica BBBV).

### Iteracja Grovera
Start: $|s\rangle=H^{\otimes n}|0\rangle$. Powtarzamy $G=D\,O$:
- **wyrocznia** $O|x\rangle=(-1)^{f(x)}|x\rangle$ — odwraca znak rozwiązania,
- **dyfuzja** $D=2|s\rangle\langle s|-I$ — odbija wszystkie amplitudy względem średniej.

Geometrycznie dwa odbicia dają **obrót** o kąt $2\theta$ w płaszczyźnie rozpiętej przez $|x^*\rangle$ i resztę stanów, gdzie $\sin\theta=1/\sqrt N$. Po $k$ iteracjach amplituda rozwiązania to $\sin((2k+1)\theta)$.

Optymalna liczba iteracji:
$$k\approx\frac{\pi}{4}\sqrt{N}\qquad\Big(\text{dla }M\text{ rozwiązań: }\tfrac{\pi}{4}\sqrt{N/M}\Big).$$
Za dużo iteracji „przekręca” stan i prawdopodobieństwo sukcesu **spada**.

> [!ex] N = 4
> $\theta=30°$; po jednej iteracji kąt $3\theta=90°$ — rozwiązanie z prawdopodobieństwem 1. Zobacz preset „Grover” w symulatorze obwodów.

### Amplifikacja amplitudy
Uogólnienie: jeśli procedura $\mathcal A$ znajduje rozwiązanie z prawdopodobieństwem $p$, wystarczy $O(1/\sqrt p)$ powtórzeń z odbiciami zamiast $O(1/p)$. Używane m.in. w kwantowym zliczaniu, estymacji amplitudy (Monte Carlo z przyspieszeniem kwadratowym) i niektórych algorytmach QML.

### Ograniczenia
- Przyspieszenie jest tylko kwadratowe; po doliczeniu narzutu korekcji błędów praktyczna przewaga wymaga ogromnych $N$.
- Potrzebna efektywna wyrocznia; „baza danych” musiałaby być dostępna w superpozycji (problem QRAM).
- Kryptografia symetryczna: Grover osłabia AES‑128 do ok. 64 bitów bezpieczeństwa → zaleca się AES‑256.

@@@ fiszki
Q: Ile zapytań potrzebuje algorytm Grovera dla $N$ elementów i jednego rozwiązania?
A: Około $\frac{\pi}{4}\sqrt N$, czyli $O(\sqrt N)$.

Q: Z jakich dwóch odbić składa się iteracja Grovera?
A: Z wyroczni (odwrócenie znaku rozwiązania) i dyfuzji $2|s\rangle\langle s|-I$ (odbicie względem średniej).

Q: Co się stanie, gdy wykonamy za dużo iteracji Grovera?
A: Stan „przekręci się” poza rozwiązanie i prawdopodobieństwo sukcesu zacznie maleć (zachowanie okresowe).

Q: Czy przyspieszenie Grovera da się poprawić?
A: Nie — $\Omega(\sqrt N)$ zapytań to dolna granica dla przeszukiwania nieuporządkowanego.

Q: Na czym polega amplifikacja amplitudy?
A: Zwiększa prawdopodobieństwo sukcesu procedury z $p$ do prawie 1 w $O(1/\sqrt p)$ krokach zamiast $O(1/p)$.

Q: Jak Grover wpływa na AES?
A: Kwadratowo skraca przeszukiwanie klucza: AES‑128 ma ok. 64 bity bezpieczeństwa kwantowego, stąd zalecenie AES‑256.

@@@ quiz
? Ile iteracji Grovera jest optymalne dla $N=2^{20}$ i jednego rozwiązania?
- około 20
+ około 804
- około $10^6$
- około $5\cdot10^5$
! $\frac{\pi}{4}\sqrt{2^{20}}=\frac{\pi}{4}\cdot1024\approx804$.

? Jaki rodzaj przyspieszenia daje Grover?
+ Kwadratowy
- Wykładniczy
- Logarytmiczny
- Żaden
! $O(\sqrt N)$ zamiast $O(N)$.

? Ile iteracji potrzeba dla $N=4$ i jednego rozwiązania?
+ 1
- 2
- 4
- 0
! $\sin\theta=1/2$, więc $\theta=30°$; po jednej iteracji $3\theta=90°$.

? Operator dyfuzji Grovera to:
- $H^{\otimes n}$
+ $2|s\rangle\langle s|-I$
- $I-2|x^*\rangle\langle x^*|$
- QFT
! Odbicie względem stanu równomiernej superpozycji (inwersja wokół średniej).
%%%
---
id: alg-teleport
title: Teleportacja i kodowanie supergęste
summary: Protokoły komunikacyjne oparte na splątaniu: 2 bity ↔ 1 kubit.
level: średni
lab: circuit
---
### Teleportacja kwantowa
Alicja chce przekazać Bobowi nieznany stan $|\psi\rangle$. Mają wspólną parę Bella $|\Phi^+\rangle$ i kanał klasyczny.
1. Alicja wykonuje CNOT ($|\psi\rangle$ → jej połowa pary) i $H$ na kubicie z $|\psi\rangle$.
2. Mierzy oba swoje kubity → 2 bity $(m_1,m_2)$.
3. Wysyła bity Bobowi; Bob stosuje $X^{m_2}$, a potem $Z^{m_1}$ → ma $|\psi\rangle$.

Bilans: **1 para Bella + 2 bity klasyczne → 1 kubit**. Nic nie dzieje się szybciej niż światło (Bob musi czekać na bity), a oryginał u Alicji zostaje zniszczony — zgodnie z twierdzeniem o nieklonowaniu.

### Kodowanie supergęste
Odwrotność teleportacji: **1 para Bella + 1 przesłany kubit → 2 bity klasyczne**. Alicja na swojej połowie pary stosuje $I$, $X$, $Z$ lub $XZ$ (otrzymując jeden z 4 stanów Bella) i wysyła kubit; Bob wykonuje CNOT, $H$ i mierzy.

### Zasada odroczonego pomiaru
Pomiar + bramkę sterowaną klasycznie można zastąpić bramką sterowaną kwantowo i pomiarem na końcu. W symulatorze teleportację realizujemy bramkami CNOT i CZ zamiast korekt klasycznych.

### Gdzie to się przydaje?
Sieci kwantowe i **kwantowy internet**, przesyłanie stanów między modułami procesora, bramki teleportowane w obliczeniach odpornych na błędy (np. *magic state injection* dla bramki $T$).

@@@ fiszki
Q: Jakie zasoby zużywa teleportacja jednego kubitu?
A: Jedną parę Bella i dwa bity klasyczne.

Q: Jakie korekty stosuje Bob w teleportacji?
A: $X^{m_2}$ i $Z^{m_1}$ — zależnie od dwóch bitów z pomiaru Alicji.

Q: Czy teleportacja narusza twierdzenie o nieklonowaniu?
A: Nie — stan Alicji ulega zniszczeniu przy pomiarze; przez cały czas istnieje tylko jedna kopia.

Q: Na czym polega kodowanie supergęste?
A: Wysłanie 1 kubitu (z wcześniej dzielonej pary Bella) przenosi 2 bity informacji klasycznej.

Q: Co mówi zasada odroczonego pomiaru?
A: Pomiar ze sterowaniem klasycznym można zastąpić bramką sterowaną kwantowo i przenieść pomiar na koniec obwodu.

@@@ quiz
? Ile bitów klasycznych musi wysłać Alicja w teleportacji?
- 0
- 1
+ 2
- zależy od stanu
! Dwa wyniki pomiarów wskazują jedną z 4 korekt.

? Dlaczego teleportacja nie pozwala komunikować się szybciej od światła?
+ Bob potrzebuje bitów klasycznych od Alicji, by odtworzyć stan
- Bo stan Bella jest nietrwały
- Bo foton porusza się wolniej niż światło
- Bo pomiar trwa nieskończenie długo
! Bez korekt stan Boba jest maksymalnie mieszany.

? Kodowanie supergęste pozwala przesłać za pomocą jednego kubitu:
- 1 bit
+ 2 bity (przy wcześniej dzielonej parze splątanej)
- dowolnie wiele bitów
- pół bita
! Ograniczenie Holevo jest tu „obchodzone” dzięki wcześniej dzielonemu splątaniu.
%%%
---
id: alg-vqa
title: Algorytmy wariacyjne: VQE i QAOA
summary: Hybrydowa pętla kwantowo‑klasyczna — wspólny fundament z QML.
level: średni
---
### Idea
Obwód parametryzowany $U(\boldsymbol\theta)$ przygotowuje stan $|\psi(\boldsymbol\theta)\rangle$; komputer kwantowy mierzy funkcję kosztu, a klasyczny optymalizator aktualizuje $\boldsymbol\theta$. Płytkie obwody lepiej znoszą szum ery NISQ.

### VQE (Variational Quantum Eigensolver)
Zasada wariacyjna: dla dowolnego stanu
$$E(\boldsymbol\theta)=\langle\psi(\boldsymbol\theta)|H|\psi(\boldsymbol\theta)\rangle\ \ge\ E_0.$$
Minimalizując $E(\boldsymbol\theta)$, przybliżamy energię stanu podstawowego (chemia, fizyka materiałów). Hamiltonian $H=\sum_jc_jP_j$ mierzy się człon po członie.

Wybór **ansatzu**:
- **UCCSD** — inspirowany chemią (fizycznie sensowny, ale głęboki),
- **hardware‑efficient** — warstwy rotacji i CNOT‑ów dopasowane do sprzętu (płytki, ale grozi barren plateaus).

### QAOA (Quantum Approximate Optimization Algorithm)
Dla problemów kombinatorycznych (MaxCut, QUBO) koszt zapisujemy jako diagonalny hamiltonian $H_C$, np. $\sum_{(i,j)\in E}\frac{1}{2}(1-Z_iZ_j)$ dla MaxCut. Stan:
$$|\boldsymbol\gamma,\boldsymbol\beta\rangle=\prod_{l=1}^{p}e^{-i\beta_lH_M}e^{-i\gamma_lH_C}|+\rangle^{\otimes n},\qquad H_M=\sum_iX_i.$$
Dla $p\to\infty$ QAOA odtwarza obliczenia adiabatyczne. Przewagi nad dobrymi heurystykami klasycznymi jak dotąd nie pokazano przekonująco.

### Wyzwania wspólne z QML
- **barren plateaus** — znikające gradienty,
- koszt pomiarów (shotów) i szum,
- lokalne minima i wybór inicjalizacji,
- brak gwarancji przewagi.

> [!key] Most do QML
> Wariacyjny klasyfikator kwantowy to „VQE dla danych”: zamiast energii minimalizujemy stratę na zbiorze treningowym, a dane wchodzą do obwodu przez kodowanie.

@@@ fiszki
Q: Na czym opiera się VQE?
A: Na zasadzie wariacyjnej: $\langle\psi(\boldsymbol\theta)|H|\psi(\boldsymbol\theta)\rangle\ge E_0$; minimalizujemy energię po parametrach obwodu.

Q: Opisz pętlę hybrydową algorytmów wariacyjnych.
A: QPU przygotowuje stan i mierzy koszt → CPU (optymalizator) aktualizuje parametry → powtarzamy.

Q: Jak wygląda stan QAOA o głębokości $p$?
A: $\prod_{l}e^{-i\beta_lH_M}e^{-i\gamma_lH_C}|+\rangle^{\otimes n}$, z mikserem $H_M=\sum_iX_i$.

Q: Jaki hamiltonian kosztu ma MaxCut w QAOA?
A: $H_C=\sum_{(i,j)\in E}\frac12(1-Z_iZ_j)$ — liczy przecięte krawędzie.

Q: Czym różni się ansatz UCCSD od hardware‑efficient?
A: UCCSD wynika z chemii (dokładny, głęboki), hardware‑efficient jest dopasowany do bramek i łączności sprzętu (płytki, ale podatny na barren plateaus).

@@@ quiz
? Co gwarantuje zasada wariacyjna w VQE?
+ Że zmierzona energia nie jest mniejsza od energii stanu podstawowego
- Że optymalizator znajdzie minimum globalne
- Że obwód nie ma szumu
- Że energia maleje w każdej iteracji
! $E(\boldsymbol\theta)\ge E_0$ dla każdego stanu próbnego.

? Który element QAOA pełni rolę „miksera”?
- $e^{-i\gamma H_C}$
+ $e^{-i\beta\sum_iX_i}$
- pomiar w bazie Z
- stan $|0\rangle^{\otimes n}$
! Mikser przenosi amplitudę między rozwiązaniami.

? Jaka jest główna zaleta algorytmów wariacyjnych w erze NISQ?
- Gwarantowana przewaga kwantowa
+ Płytkie obwody i część obliczeń przeniesiona na komputer klasyczny
- Brak potrzeby pomiarów
- Brak parametrów do optymalizacji
! Krótkie obwody lepiej znoszą dekoherencję.
%%%
---
id: alg-complexity
title: Złożoność, przewaga kwantowa i HHL
summary: BQP, eksperymenty „supremacji”, HHL z drobnym drukiem i rodzaje przyspieszeń.
level: zaawansowany
---
### Klasy złożoności
- **BQP** — problemy rozwiązywalne przez komputer kwantowy w czasie wielomianowym z prawdopodobieństwem błędu co najwyżej 1/3.
- Wiadomo, że $\mathrm{BPP}\subseteq\mathrm{BQP}\subseteq\mathrm{PSPACE}$.
- Faktoryzacja należy do BQP, ale nie wiadomo, czy jest NP‑zupełna (raczej nie jest).
- Powszechne przekonanie: komputery kwantowe **nie** rozwiązują efektywnie problemów NP‑zupełnych (np. SAT) — Grover daje tylko pierwiastek.

### Rodzaje przyspieszeń
| Typ | Przykład |
|---|---|
| wykładnicze / superwielomianowe | Shor, symulacja układów kwantowych |
| kwadratowe | Grover, estymacja amplitudy (Monte Carlo) |
| niewielkie wielomianowe | niektóre algorytmy grafowe |
| heurystyczne, niepewne | VQE, QAOA, większość QML |

### Eksperymenty przewagi
- **Google Sycamore (2019)**: próbkowanie losowych obwodów na 53 kubitach; deklarowane 200 s vs 10 000 lat — później klasyczne symulacje znacznie zmniejszyły tę lukę.
- **Jiuzhang (2020)**: boson sampling na fotonach.
- **Google Willow (2024)**: próbkowanie daleko poza zasięgiem symulacji oraz korekcja błędów poniżej progu.

Wniosek: przewaga dla **sztucznych** zadań próbkowania jest dobrze uzasadniona; dla **użytecznych** problemów trwają badania.

### HHL (Harrow–Hassidim–Lloyd, 2009)
Rozwiązuje $A\vec x=\vec b$, przygotowując stan $|x\rangle\propto A^{-1}|b\rangle$ w czasie $\tilde O(\log N\,s^2\kappa^2/\varepsilon)$. **Drobny druk** (Aaronson, „Read the fine print”):
1. trzeba efektywnie przygotować $|b\rangle$ (QRAM),
2. $A$ musi być rzadka i dobrze uwarunkowana (małe $\kappa$),
3. wynikiem jest stan kwantowy — odczyt wszystkich $N$ składowych zabija przewagę; można tylko estymować wielkości typu $\langle x|M|x\rangle$.

HHL to fundament „pierwszej fali” QML (kwantowe SVM, regresja, PCA) — i właśnie te założenia doprowadziły do **dekwantyzacji** (zob. dział QML).

@@@ fiszki
Q: Czym jest klasa BQP?
A: Zbiorem problemów decyzyjnych rozwiązywalnych przez komputer kwantowy w czasie wielomianowym z prawdopodobieństwem błędu co najwyżej 1/3.

Q: Jakie inkluzje dla BQP są udowodnione?
A: $\mathrm{BPP}\subseteq\mathrm{BQP}\subseteq\mathrm{PSPACE}$.

Q: Czy komputery kwantowe rozwiązują efektywnie problemy NP‑zupełne?
A: Nie wiadomo, ale powszechnie uważa się, że nie; Grover daje tylko przyspieszenie kwadratowe.

Q: Co oblicza HHL i jakie ma ograniczenia?
A: Stan $|x\rangle\propto A^{-1}|b\rangle$; wymaga efektywnego przygotowania $|b\rangle$, rzadkiej i dobrze uwarunkowanej $A$, a wynik jest stanem (nie wektorem do odczytu).

Q: Jakie zadanie wykorzystał Google w 2019 r. do demonstracji przewagi?
A: Próbkowanie z losowych obwodów kwantowych (random circuit sampling) na 53 kubitach.

@@@ quiz
? Która inkluzja jest udowodniona?
+ $\mathrm{BPP}\subseteq\mathrm{BQP}$
- $\mathrm{NP}\subseteq\mathrm{BQP}$
- $\mathrm{BQP}\subseteq\mathrm{NP}$
- $\mathrm{BQP}=\mathrm{P}$
! Komputer kwantowy może symulować obliczenia losowe; pozostałe relacje są otwartymi problemami.

? Który element NIE należy do „drobnego druku” HHL?
- Potrzeba efektywnego przygotowania $|b\rangle$
- Mała liczba uwarunkowania $\kappa$
- Wynik jako stan kwantowy, którego nie da się tanio odczytać
+ Konieczność, by $A$ była unitarna
! $A$ ma być hermitowska (lub uzupełniona do takiej), rzadka i dobrze uwarunkowana — nie unitarna.

? Jakie przyspieszenie daje kwantowa estymacja amplitudy dla metod Monte Carlo?
- Wykładnicze
+ Kwadratowe
- Żadne
- Logarytmiczne
! Błąd $O(1/N)$ zamiast $O(1/\sqrt N)$ dla $N$ próbek.
