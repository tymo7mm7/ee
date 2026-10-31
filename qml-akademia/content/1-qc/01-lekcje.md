---
id: qc-kubit
title: Kubit i superpozycja
summary: Czym kubit różni się od bitu, notacja Diraca i reguła Borna.
level: podstawowy
lab: bloch
---
Klasyczny bit ma wartość 0 albo 1. **Kubit** (bit kwantowy) to układ dwupoziomowy, którego stan opisuje wektor w dwuwymiarowej zespolonej przestrzeni Hilberta $\mathbb{C}^2$.

### Notacja Diraca
Stany bazowe (baza obliczeniowa):
$$|0\rangle=\begin{pmatrix}1\\0\end{pmatrix},\qquad |1\rangle=\begin{pmatrix}0\\1\end{pmatrix}$$
Ogólny stan kubitu to **superpozycja**:
$$|\psi\rangle=\alpha|0\rangle+\beta|1\rangle,\qquad \alpha,\beta\in\mathbb{C},\quad |\alpha|^2+|\beta|^2=1.$$
- **ket** $|\psi\rangle$ — wektor kolumnowy,
- **bra** $\langle\psi|=|\psi\rangle^\dagger$ — wektor wierszowy ze sprzężonymi liczbami,
- **iloczyn skalarny** $\langle\phi|\psi\rangle$ — liczba zespolona; $|\langle\phi|\psi\rangle|^2$ mówi, jak bardzo stany się „pokrywają”.

### Reguła Borna
Pomiar w bazie obliczeniowej daje wynik 0 z prawdopodobieństwem $|\alpha|^2$ i 1 z prawdopodobieństwem $|\beta|^2$. Po pomiarze stan **zapada się** do $|0\rangle$ lub $|1\rangle$ — informacja o amplitudach przepada.

> [!warn] Superpozycja ≠ „jednocześnie 0 i 1” w sensie klasycznym
> Kubit nie przechowuje dwóch wartości do odczytania. Pomiar daje jeden bit. Siła obliczeń kwantowych bierze się z **interferencji** amplitud (mogą się dodawać lub znosić), a nie z „liczenia wszystkiego naraz”.

### Amplitudy są zespolone
Amplituda ma moduł i fazę: $\alpha=|\alpha|e^{i\varphi_\alpha}$. Stany
$$|+\rangle=\tfrac{1}{\sqrt2}(|0\rangle+|1\rangle),\qquad |-\rangle=\tfrac{1}{\sqrt2}(|0\rangle-|1\rangle)$$
dają **identyczne** statystyki pomiaru w bazie $\{|0\rangle,|1\rangle\}$ (50/50), a mimo to są ortogonalne: $\langle+|-\rangle=0$. Różni je faza względna, którą wykryjemy, mierząc w innej bazie.

### Ile informacji mieści kubit?
Opis stanu wymaga dwóch liczb rzeczywistych, ale **twierdzenie Holevo** mówi, że z $n$ kubitów można odczytać co najwyżej $n$ bitów klasycznej informacji. Rejestr $n$ kubitów opisuje $2^n$ amplitud — to źródło trudności symulacji klasycznej, ale nie „darmowa pamięć”.

> [!key]
> - Stan kubitu: $\alpha|0\rangle+\beta|1\rangle$, $|\alpha|^2+|\beta|^2=1$.
> - Prawdopodobieństwo wyniku = kwadrat modułu amplitudy (reguła Borna).
> - Pomiar niszczy superpozycję; kluczowa jest interferencja.

@@@ fiszki
Q: Czym jest kubit?
A: Kwantowym układem dwupoziomowym; jego stan to unormowany wektor $\alpha|0\rangle+\beta|1\rangle$ w $\mathbb{C}^2$.

Q: Jaki warunek muszą spełniać amplitudy $\alpha,\beta$ stanu kubitu?
A: Normalizacja: $|\alpha|^2+|\beta|^2=1$.

Q: Co mówi reguła Borna?
A: Prawdopodobieństwo wyniku $i$ to kwadrat modułu amplitudy: $p(i)=|\langle i|\psi\rangle|^2$.

Q: Zapisz stany $|+\rangle$ i $|-\rangle$.
A: $|\pm\rangle=\frac{1}{\sqrt2}(|0\rangle\pm|1\rangle)$ — baza Hadamarda (baza $X$).

Q: Co mówi twierdzenie Holevo?
A: Z $n$ kubitów można odczytać co najwyżej $n$ bitów klasycznej informacji, choć stan opisuje $2^n$ amplitud.

Q: Czym różni się bra od keta?
A: Ket $|\psi\rangle$ to wektor kolumnowy; bra $\langle\psi|=|\psi\rangle^\dagger$ to wektor wierszowy ze sprzężonymi elementami.

@@@ quiz
? Kubit jest w stanie $\frac{1}{\sqrt3}|0\rangle+\sqrt{\frac{2}{3}}|1\rangle$. Jakie jest prawdopodobieństwo wyniku 1?
- 1/3
+ 2/3
- $\sqrt{2/3}$
- 1/2
! Reguła Borna: $p(1)=|\beta|^2=2/3$.

? Które zdanie o stanach $|+\rangle$ i $|-\rangle$ jest prawdziwe?
- Są identyczne, bo dają te same wyniki w bazie Z
+ Są ortogonalne, choć w bazie Z dają te same statystyki
- Różnią się tylko fazą globalną
- $|-\rangle$ nie jest poprawnym stanem, bo ma ujemną amplitudę
! $\langle+|-\rangle=0$. Różni je faza **względna**, widoczna np. po pomiarze w bazie X (po bramce H).

? Co dzieje się ze stanem kubitu po pomiarze w bazie obliczeniowej?
- Nic, pomiar tylko odczytuje amplitudy
+ Zapada się do stanu bazowego odpowiadającego wynikowi
- Zamienia się w stan $|+\rangle$
- Amplitudy zostają przemnożone przez 1/2
! Pomiar projekcyjny „kolapsuje” stan do $|0\rangle$ lub $|1\rangle$.

? Ile bitów klasycznej informacji można niezawodnie odczytać z 1 kubitu?
- 0
+ 1
- 2
- nieskończenie wiele, bo amplitudy są liczbami rzeczywistymi
! Ograniczenie Holevo: co najwyżej 1 bit na kubit.
%%%
---
id: qc-bloch
title: Sfera Blocha, faza globalna i względna
summary: Geometryczny obraz stanu kubitu — każdy stan czysty to punkt na sferze.
level: podstawowy
lab: bloch
---
Ponieważ $|\alpha|^2+|\beta|^2=1$, a **faza globalna** nie ma znaczenia fizycznego, każdy stan czysty kubitu można zapisać jako
$$|\psi\rangle=\cos\tfrac{\theta}{2}\,|0\rangle+e^{i\varphi}\sin\tfrac{\theta}{2}\,|1\rangle,\qquad \theta\in[0,\pi],\ \varphi\in[0,2\pi).$$
Kąty $(\theta,\varphi)$ to współrzędne sferyczne punktu na **sferze Blocha**:
$$\vec r=(\sin\theta\cos\varphi,\ \sin\theta\sin\varphi,\ \cos\theta).$$

### Ważne punkty
| Stan | Położenie | Wektor $\vec r$ |
|---|---|---|
| $\lvert 0\rangle$ | biegun północny | $(0,0,1)$ |
| $\lvert 1\rangle$ | biegun południowy | $(0,0,-1)$ |
| $\lvert +\rangle$ | równik, oś $+x$ | $(1,0,0)$ |
| $\lvert -\rangle$ | równik, oś $-x$ | $(-1,0,0)$ |
| $\lvert +i\rangle$ | równik, oś $+y$ | $(0,1,0)$ |
| $\lvert -i\rangle$ | równik, oś $-y$ | $(0,-1,0)$ |

Stany **ortogonalne** leżą w punktach antypodycznych (180° na sferze, choć „90°” w przestrzeni Hilberta — stąd $\theta/2$ we wzorze).

### Faza globalna vs względna
- **Faza globalna**: $e^{i\gamma}|\psi\rangle$ i $|\psi\rangle$ to ten sam stan fizyczny — żaden pomiar ich nie odróżni.
- **Faza względna** $\varphi$ między $|0\rangle$ i $|1\rangle$ zmienia stan (obrót wokół osi $z$) i jest kluczowa dla interferencji.

### Wartości oczekiwane
Składowe wektora Blocha to wartości oczekiwane macierzy Pauliego:
$$x=\langle X\rangle,\quad y=\langle Y\rangle,\quad z=\langle Z\rangle=p(0)-p(1).$$
To ważne w QML: model kwantowy często zwraca właśnie $\langle Z\rangle\in[-1,1]$.

### Stany mieszane
Stany czyste leżą **na** sferze ($|\vec r|=1$). Stany mieszane (np. kubit splątany z innym albo zaszumiony) leżą **wewnątrz** kuli: $|\vec r|<1$. Środek kuli to stan maksymalnie mieszany $I/2$.

> [!tip] Wypróbuj
> W laboratorium „Sfera Blocha” nałóż $H$, a potem $S$ — zobaczysz drogę $|0\rangle\to|+\rangle\to|+i\rangle$.

@@@ fiszki
Q: Zapisz ogólny stan kubitu w parametryzacji sfery Blocha.
A: $|\psi\rangle=\cos\frac{\theta}{2}|0\rangle+e^{i\varphi}\sin\frac{\theta}{2}|1\rangle$.

Q: Gdzie na sferze Blocha leżą $|0\rangle$, $|1\rangle$, $|+\rangle$, $|+i\rangle$?
A: $|0\rangle$ — biegun północny, $|1\rangle$ — południowy, $|+\rangle$ — oś $+x$, $|+i\rangle$ — oś $+y$ (równik).

Q: Czym różni się faza globalna od względnej?
A: Globalna ($e^{i\gamma}$ przed całym stanem) jest niemierzalna. Względna (między $|0\rangle$ i $|1\rangle$) zmienia stan — to obrót wokół osi $z$.

Q: Jak składowe wektora Blocha wiążą się z pomiarami?
A: $x=\langle X\rangle$, $y=\langle Y\rangle$, $z=\langle Z\rangle=p(0)-p(1)$.

Q: Gdzie leżą stany mieszane?
A: Wewnątrz kuli Blocha ($|\vec r|<1$); środek to stan maksymalnie mieszany $I/2$.

@@@ quiz
? Gdzie na sferze Blocha leżą dwa stany ortogonalne?
- W tym samym punkcie
- Pod kątem 90°
+ W punktach antypodycznych (naprzeciw siebie)
- Zawsze na równiku
! Ortogonalne stany dzieli 180° na sferze — stąd czynnik $\theta/2$ w parametryzacji.

? Stan ma wektor Blocha $(0,0,0.6)$. Co można powiedzieć?
- To stan czysty bliski $|0\rangle$
+ To stan mieszany; $p(0)=0.8$
- To stan $|+\rangle$
- Taki wektor nie jest możliwy
! $|\vec r|=0.6<1$, więc stan mieszany. $p(0)=(1+z)/2=0.8$.

? Która operacja NIE zmienia stanu fizycznego?
+ Pomnożenie całego stanu przez $e^{i\pi/3}$
- Zmiana znaku amplitudy przy $|1\rangle$
- Zamiana amplitud $\alpha$ i $\beta$
- Pomnożenie amplitudy przy $|1\rangle$ przez $i$
! Faza globalna jest niemierzalna. Pozostałe operacje zmieniają fazę względną lub populacje.

? Ile wynosi $\langle Z\rangle$ dla stanu $|+\rangle$?
- 1
- −1
+ 0
- 1/2
! $|+\rangle$ leży na równiku: $p(0)=p(1)=1/2$, więc $\langle Z\rangle=0$.
%%%
---
id: qc-gates1
title: Bramki jednokubitowe
summary: Macierze Pauliego, Hadamard, fazy S i T oraz rotacje — wszystkie jako obroty sfery Blocha.
level: podstawowy
lab: bloch
---
Ewolucja zamkniętego układu kwantowego jest **unitarna**: $|\psi\rangle\mapsto U|\psi\rangle$, gdzie $U^\dagger U=I$. Unitarność zachowuje normę (prawdopodobieństwa sumują się do 1) i oznacza **odwracalność** obliczeń.

### Macierze Pauliego
$$X=\begin{pmatrix}0&1\\1&0\end{pmatrix},\quad Y=\begin{pmatrix}0&-i\\i&0\end{pmatrix},\quad Z=\begin{pmatrix}1&0\\0&-1\end{pmatrix}$$
- $X$ — kwantowe NOT: $|0\rangle\leftrightarrow|1\rangle$ (obrót o $\pi$ wokół osi $x$),
- $Z$ — odwrócenie fazy: $|1\rangle\mapsto-|1\rangle$ (obrót o $\pi$ wokół $z$),
- $Y=iXZ$ — obrót o $\pi$ wokół $y$.

Własności: $X^2=Y^2=Z^2=I$, są hermitowskie i unitarne, parami antykomutują (np. $XZ=-ZX$).

### Hadamard
$$H=\frac{1}{\sqrt2}\begin{pmatrix}1&1\\1&-1\end{pmatrix},\qquad H|0\rangle=|+\rangle,\quad H|1\rangle=|-\rangle$$
Zamienia bazy $Z\leftrightarrow X$: $HZH=X$. Na sferze to obrót o $\pi$ wokół osi $(x+z)/\sqrt2$. Jest swoją odwrotnością: $H^2=I$.

### Bramki fazowe
$$S=\begin{pmatrix}1&0\\0&i\end{pmatrix},\quad T=\begin{pmatrix}1&0\\0&e^{i\pi/4}\end{pmatrix},\quad P(\lambda)=\begin{pmatrix}1&0\\0&e^{i\lambda}\end{pmatrix}$$
$T^2=S$, $S^2=Z$. Obracają stan wokół osi $z$ odpowiednio o $\pi/4$, $\pi/2$, $\lambda$.

### Rotacje
$$R_X(\theta)=e^{-i\theta X/2},\qquad R_Y(\theta)=\begin{pmatrix}\cos\frac{\theta}{2}&-\sin\frac{\theta}{2}\\ \sin\frac{\theta}{2}&\cos\frac{\theta}{2}\end{pmatrix},\qquad R_Z(\theta)=\begin{pmatrix}e^{-i\theta/2}&0\\0&e^{i\theta/2}\end{pmatrix}$$
$R_\sigma(\theta)$ obraca wektor Blocha o kąt $\theta$ wokół osi $\sigma$. To podstawowe **bramki parametryzowane** w QML — ich kąty to trenowane wagi albo zakodowane dane.

> [!info] Dowolna bramka jednokubitowa
> Każde $U\in U(2)$ da się zapisać (z dokładnością do fazy globalnej) jako $R_Z(\alpha)R_Y(\beta)R_Z(\gamma)$ — rozkład Eulera. Qiskit udostępnia ogólną bramkę $U(\theta,\varphi,\lambda)$.

### Przydatne tożsamości
- $HXH=Z$, $HZH=X$, $HYH=-Y$
- $R_Z(\theta)$ i $P(\theta)$ różnią się tylko fazą globalną $e^{-i\theta/2}$
- $XR_Y(\theta)X=R_Y(-\theta)$

@@@ fiszki
Q: Podaj macierze $X$, $Z$ i $H$.
A: $X=\begin{pmatrix}0&1\\1&0\end{pmatrix}$, $Z=\begin{pmatrix}1&0\\0&-1\end{pmatrix}$, $H=\frac{1}{\sqrt2}\begin{pmatrix}1&1\\1&-1\end{pmatrix}$.

Q: Dlaczego bramki kwantowe muszą być unitarne?
A: Ewolucja zamkniętego układu jest unitarna: zachowuje normę (sumę prawdopodobieństw) i jest odwracalna, $U^\dagger U=I$.

Q: Co robi bramka $R_Y(\theta)$ na sferze Blocha?
A: Obraca wektor Blocha o kąt $\theta$ wokół osi $y$; $R_Y(\theta)=e^{-i\theta Y/2}$.

Q: Jaki jest związek między $T$, $S$ i $Z$?
A: $T^2=S$, $S^2=Z$ (obroty wokół $z$ o $\pi/4$, $\pi/2$, $\pi$).

Q: Ile wynosi $HZH$?
A: $X$ — Hadamard zamienia bazy $Z$ i $X$.

Q: Co mówi rozkład Eulera dla bramek jednokubitowych?
A: Każdą bramkę jednokubitową można zapisać jako $R_Z(\alpha)R_Y(\beta)R_Z(\gamma)$ (z dokładnością do fazy globalnej).

@@@ quiz
? Jaki stan otrzymamy, działając $H$ na $|1\rangle$?
- $|+\rangle$
+ $|-\rangle$
- $|0\rangle$
- $|+i\rangle$
! $H|1\rangle=\frac{1}{\sqrt2}(|0\rangle-|1\rangle)=|-\rangle$.

? Która bramka odpowiada obrotowi o $\pi/2$ wokół osi $z$?
- $T$
+ $S$
- $Z$
- $H$
! $S=\mathrm{diag}(1,i)$ to obrót o $\pi/2$; $T$ — o $\pi/4$, $Z$ — o $\pi$.

? Ile wynosi $R_Y(\pi)|0\rangle$?
- $|0\rangle$
+ $|1\rangle$
- $|+\rangle$
- $-|0\rangle$
! Obrót o 180° wokół $y$ przenosi biegun północny na południowy: $\cos\frac{\pi}{2}|0\rangle+\sin\frac{\pi}{2}|1\rangle=|1\rangle$.

? Która własność NIE dotyczy macierzy Pauliego?
- $\sigma^2=I$
- Są hermitowskie
- Są unitarne
+ Komutują ze sobą parami
! Macierze Pauliego antykomutują, np. $XZ=-ZX$.

? Ile wynosi $R_Y(\pi/2)|0\rangle$?
+ $|+\rangle$
- $|-\rangle$
- $|+i\rangle$
- $|1\rangle$
! $\cos\frac{\pi}{4}|0\rangle+\sin\frac{\pi}{4}|1\rangle=|+\rangle$ — obrót z bieguna na oś $+x$.
%%%
---
id: qc-multi
title: Wiele kubitów i iloczyn tensorowy
summary: Rejestr n kubitów żyje w przestrzeni o wymiarze 2ⁿ — stąd moc i trudność symulacji.
level: podstawowy
lab: circuit
---
Stan układu złożonego opisuje **iloczyn tensorowy** (Kroneckera). Dla dwóch kubitów:
$$|a\rangle\otimes|b\rangle=|ab\rangle,\qquad \begin{pmatrix}a_0\\a_1\end{pmatrix}\otimes\begin{pmatrix}b_0\\b_1\end{pmatrix}=\begin{pmatrix}a_0b_0\\a_0b_1\\a_1b_0\\a_1b_1\end{pmatrix}.$$
Baza dwukubitowa: $|00\rangle,|01\rangle,|10\rangle,|11\rangle$. Ogólny stan $n$ kubitów:
$$|\psi\rangle=\sum_{x\in\{0,1\}^n}c_x|x\rangle,\qquad \sum_x|c_x|^2=1.$$

### Wykładniczy wymiar
$n$ kubitów → $2^n$ amplitud zespolonych. 50 kubitów to $2^{50}\approx10^{15}$ amplitud (petabajty pamięci przy pełnej symulacji). To główny powód, dla którego **ogólne** obwody kwantowe trudno symulować klasycznie.

### Bramki na wielu kubitach
Bramka $U$ na pierwszym kubicie i nic na drugim to $U\otimes I$. Bramki równoległe: $(A\otimes B)(|a\rangle\otimes|b\rangle)=A|a\rangle\otimes B|b\rangle$. Przykład:
$$H^{\otimes n}|0\rangle^{\otimes n}=\frac{1}{\sqrt{2^n}}\sum_{x}|x\rangle$$
— równomierna superpozycja wszystkich $2^n$ ciągów bitów; punkt startowy wielu algorytmów.

### Stany produktowe
Stan jest **produktowy**, jeśli da się go zapisać jako $|a\rangle\otimes|b\rangle$. Np. $\frac{1}{2}(|00\rangle+|01\rangle+|10\rangle+|11\rangle)=|+\rangle|+\rangle$. Stany, których tak zapisać się nie da, są **splątane** (następna lekcja).

> [!warn] Kolejność kubitów (endianness)
> W podręcznikach i PennyLane zapis to $|q_0q_1\dots\rangle$ — kubit 0 to lewy bit. **Qiskit** jest little‑endian: kubit 0 to **prawy** bit, więc $|01\rangle$ w Qiskit oznacza $q_0=1$. Porównując wyniki, zawsze sprawdź konwencję!

@@@ fiszki
Q: Jaki wymiar ma przestrzeń stanów $n$ kubitów?
A: $2^n$ (zespolona przestrzeń Hilberta $\mathbb{C}^{2^n}$).

Q: Oblicz $|0\rangle\otimes|1\rangle$ jako wektor.
A: $(0,1,0,0)^T=|01\rangle$.

Q: Co daje $H^{\otimes n}|0\dots0\rangle$?
A: Równomierną superpozycję $\frac{1}{\sqrt{2^n}}\sum_x|x\rangle$ wszystkich $2^n$ stanów bazowych.

Q: Kiedy stan dwóch kubitów jest produktowy?
A: Gdy można go zapisać jako $|a\rangle\otimes|b\rangle$; w przeciwnym razie jest splątany.

Q: Czym różni się konwencja kolejności kubitów w Qiskit?
A: Qiskit jest little‑endian: kubit 0 to najmniej znaczący (prawy) bit w zapisie $|\dots q_1q_0\rangle$.

@@@ quiz
? Ile liczb zespolonych potrzeba do zapisania ogólnego stanu 10 kubitów?
- 10
- 20
- 100
+ 1024
! $2^{10}=1024$ amplitud.

? Który stan jest produktowy?
+ $\frac12(|00\rangle+|01\rangle+|10\rangle+|11\rangle)$
- $\frac{1}{\sqrt2}(|00\rangle+|11\rangle)$
- $\frac{1}{\sqrt2}(|01\rangle-|10\rangle)$
- $\frac{1}{\sqrt3}(|00\rangle+|01\rangle+|11\rangle)$
! Pierwszy to $|+\rangle\otimes|+\rangle$. Pozostałe są splątane (nie da się ich rozłożyć na iloczyn).

? Jaka macierz opisuje bramkę $X$ na drugim kubicie rejestru $|q_0q_1\rangle$?
- $X\otimes X$
+ $I\otimes X$
- $X\otimes I$
- $X\oplus I$
! Operator działający na $q_1$ stoi na drugiej pozycji iloczynu tensorowego.
%%%
---
id: qc-entangle
title: Bramki dwukubitowe i splątanie
summary: CNOT, CZ, SWAP, stany Bella i twierdzenie o nieklonowaniu.
level: podstawowy
lab: circuit
---
### CNOT (sterowane NOT)
$$\mathrm{CNOT}=\begin{pmatrix}1&0&0&0\\0&1&0&0\\0&0&0&1\\0&0&1&0\end{pmatrix},\qquad |c,t\rangle\mapsto|c,\ t\oplus c\rangle$$
Odwraca kubit docelowy, gdy kontrolny jest w $|1\rangle$. Działając na superpozycję, **tworzy splątanie**.

### Inne bramki
- **CZ** $=\mathrm{diag}(1,1,1,-1)$ — dodaje fazę $-1$ do $|11\rangle$; symetryczna (nie ma znaczenia, który kubit jest kontrolny). $\mathrm{CNOT}=(I\otimes H)\,\mathrm{CZ}\,(I\otimes H)$.
- **SWAP** — zamienia stany kubitów; równa się 3 bramkom CNOT.
- **Toffoli (CCNOT)** — NOT sterowane dwoma kubitami; uniwersalna dla odwracalnych obliczeń klasycznych.
- **CP($\lambda$)**, **CRZ**, **iSWAP**, **ECR** — natywne bramki różnych procesorów.

### Stany Bella
$$|\Phi^\pm\rangle=\frac{|00\rangle\pm|11\rangle}{\sqrt2},\qquad |\Psi^\pm\rangle=\frac{|01\rangle\pm|10\rangle}{\sqrt2}$$
Obwód: $H$ na $q_0$, potem CNOT$(q_0\to q_1)$ daje $|\Phi^+\rangle$. Cztery stany Bella tworzą bazę ortonormalną.

### Czym jest splątanie?
Stan czysty jest **splątany**, jeśli nie jest iloczynem tensorowym stanów podukładów. Skutki:
- wyniki pomiarów są skorelowane silniej, niż pozwala jakakolwiek lokalna teoria klasyczna (**nierówności Bella**; Nagroda Nobla 2022 za eksperymenty),
- każdy kubit osobno jest w stanie **mieszanym** (w $|\Phi^+\rangle$ wektor Blocha każdego kubitu ma długość 0),
- splątanie **nie pozwala** przesyłać informacji szybciej niż światło.

Miarą splątania stanu czystego jest np. **entropia splątania** $S(\rho_A)=-\mathrm{Tr}\,\rho_A\log\rho_A$ stanu zredukowanego.

### Twierdzenie o nieklonowaniu
Nie istnieje unitarna operacja $U$, która dla **dowolnego** $|\psi\rangle$ robi $U|\psi\rangle|0\rangle=|\psi\rangle|\psi\rangle$. CNOT „kopiuje” tylko stany bazowe; dla $|+\rangle$ tworzy stan Bella zamiast $|+\rangle|+\rangle$. Konsekwencje: brak „kopii zapasowej” kubitów, bezpieczeństwo kryptografii kwantowej (QKD), trudniejsza korekcja błędów.

> [!tip] Splątanie w QML
> W obwodach wariacyjnych bramki CNOT/CZ tworzą korelacje między cechami. Bez nich obwód na $n$ kubitach to $n$ niezależnych kubitów — łatwy do symulacji i mało ekspresywny. Ale nadmiar splątania sprzyja barren plateaus.

@@@ fiszki
Q: Jak działa CNOT na stanach bazowych?
A: $|c,t\rangle\mapsto|c,t\oplus c\rangle$ — odwraca cel, gdy kontrola jest w $|1\rangle$.

Q: Zapisz cztery stany Bella.
A: $|\Phi^\pm\rangle=(|00\rangle\pm|11\rangle)/\sqrt2$, $|\Psi^\pm\rangle=(|01\rangle\pm|10\rangle)/\sqrt2$.

Q: Jak przygotować $|\Phi^+\rangle$ z $|00\rangle$?
A: $H$ na pierwszym kubicie, potem CNOT (kontrola: pierwszy, cel: drugi).

Q: Co mówi twierdzenie o nieklonowaniu?
A: Nie da się skopiować nieznanego stanu kwantowego: nie istnieje unitarne $U$ z $U|\psi\rangle|0\rangle=|\psi\rangle|\psi\rangle$ dla wszystkich $|\psi\rangle$.

Q: Jaki jest stan pojedynczego kubitu z pary w stanie $|\Phi^+\rangle$?
A: Maksymalnie mieszany $I/2$ — wektor Blocha długości 0.

Q: Jak zbudować CNOT z CZ?
A: $\mathrm{CNOT}=(I\otimes H)\,\mathrm{CZ}\,(I\otimes H)$ — Hadamardy na kubicie docelowym.

Q: Czy splątanie pozwala na komunikację szybszą od światła?
A: Nie. Korelacje widać dopiero po porównaniu wyników kanałem klasycznym (twierdzenie o braku komunikacji).

@@@ quiz
? Jaki stan powstaje po CNOT$(q_0\to q_1)$ na $|+\rangle|0\rangle$?
- $|+\rangle|+\rangle$
+ $\frac{1}{\sqrt2}(|00\rangle+|11\rangle)$
- $\frac{1}{\sqrt2}(|00\rangle+|10\rangle)$
- $|+\rangle|0\rangle$
! $\frac{1}{\sqrt2}(|0\rangle+|1\rangle)|0\rangle\to\frac{1}{\sqrt2}(|00\rangle+|11\rangle)$ — stan Bella.

? Ile bramek CNOT potrzeba do zbudowania SWAP?
- 1
- 2
+ 3
- 4
! SWAP = CNOT$_{01}$ CNOT$_{10}$ CNOT$_{01}$.

? Która bramka jest symetryczna względem zamiany kubitu kontrolnego i docelowego?
- CNOT
+ CZ
- CRY
- Toffoli
! CZ $=\mathrm{diag}(1,1,1,-1)$ dodaje fazę tylko do $|11\rangle$, więc role kubitów są symetryczne.

? Para kubitów jest w stanie $|\Psi^-\rangle$. Mierzymy oba w bazie Z. Co obserwujemy?
- Zawsze te same wyniki
+ Zawsze przeciwne wyniki
- Wyniki niezależne, 25% każda para
- Zawsze 0 na pierwszym kubicie
! $|\Psi^-\rangle=(|01\rangle-|10\rangle)/\sqrt2$: możliwe są tylko wyniki 01 lub 10.
%%%
---
id: qc-measure
title: Pomiar, obserwable i wartości oczekiwane
summary: Pomiary w różnych bazach, ⟨Z⟩, shoty i szum statystyczny.
level: średni
---
### Pomiar projekcyjny
Obserwabla to operator hermitowski $O=\sum_k\lambda_kP_k$ (rozkład spektralny). Pomiar daje wartość własną $\lambda_k$ z prawdopodobieństwem $p_k=\langle\psi|P_k|\psi\rangle$, a stan przechodzi w $P_k|\psi\rangle/\sqrt{p_k}$.

### Wartość oczekiwana
$$\langle O\rangle=\langle\psi|O|\psi\rangle=\sum_k\lambda_kp_k.$$
Dla $Z$ (wartości własne $\pm1$): $\langle Z\rangle=p(0)-p(1)$. Dla wielu kubitów np. $\langle Z_0Z_1\rangle$ to parzystość: $+1$ dla $|00\rangle,|11\rangle$ i $-1$ dla $|01\rangle,|10\rangle$.

### Pomiar w innej bazie
Sprzęt zwykle mierzy tylko w bazie $Z$. Aby zmierzyć $X$, najpierw wykonujemy $H$ (bo $X=HZH$); dla $Y$ — najpierw $S^\dagger$, potem $H$.

### Shoty — szum statystyczny
Na komputerze kwantowym $\langle O\rangle$ **estymujemy** z $N$ powtórzeń obwodu (ang. *shots*):
$$\widehat{\langle Z\rangle}=\frac{n_0-n_1}{N},\qquad \mathrm{std}=\sqrt{\frac{1-\langle Z\rangle^2}{N}}\sim\frac{1}{\sqrt N}.$$
Aby zmniejszyć błąd 10×, potrzeba 100× więcej shotów. To kluczowy koszt w QML: każda ewaluacja funkcji straty i każdy gradient to tysiące uruchomień obwodu.

### Hamiltonian jako suma Pauliego
Obserwable fizyczne i chemiczne rozkłada się na sumę iloczynów Pauliego: $H=\sum_jc_jP_j$, np. $H=0.5\,Z_0Z_1-0.3\,X_0+\dots$. Wtedy $\langle H\rangle=\sum_jc_j\langle P_j\rangle$ — każdy człon (lub grupę komutujących członów) mierzy się osobno. Tak działa VQE.

> [!info] Pomiary w środku obwodu
> Nowe procesory pozwalają mierzyć w trakcie obwodu i warunkować dalsze bramki wynikiem (*mid‑circuit measurement*, *dynamic circuits*) — potrzebne m.in. w korekcji błędów i teleportacji.

@@@ fiszki
Q: Jak obliczyć wartość oczekiwaną obserwabli $O$ w stanie $|\psi\rangle$?
A: $\langle O\rangle=\langle\psi|O|\psi\rangle=\sum_k\lambda_kp_k$.

Q: Ile wynosi $\langle Z\rangle$ w zależności od prawdopodobieństw wyników?
A: $\langle Z\rangle=p(0)-p(1)$.

Q: Jak zmierzyć obserwablę $X$, mając tylko pomiar w bazie $Z$?
A: Wykonać $H$ przed pomiarem (bo $X=HZH$).

Q: Jak błąd estymacji $\langle O\rangle$ zależy od liczby shotów $N$?
A: Jak $1/\sqrt N$ — 100× więcej pomiarów daje 10× mniejszy błąd.

Q: Jaką wartość ma $\langle Z_0Z_1\rangle$ dla stanu $|01\rangle$?
A: $-1$ (nieparzysta liczba jedynek).

@@@ quiz
? Kubit zmierzono 1000 razy: 800 razy wypadło 0. Jaka jest estymata $\langle Z\rangle$?
- 0.8
+ 0.6
- 0.2
- −0.6
! $(800-200)/1000=0.6$.

? Chcemy 10 razy dokładniej oszacować $\langle Z\rangle$. Ile razy więcej shotów potrzeba?
- 10
+ 100
- $\sqrt{10}$
- 1000
! Błąd maleje jak $1/\sqrt N$.

? Która sekwencja pozwala zmierzyć obserwablę $Y$ pomiarem w bazie $Z$?
- $H$
+ $S^\dagger$, potem $H$
- $T$, potem $H$
- $S$, potem $S$
! $S^\dagger$ obraca oś $y$ na $x$, a $H$ zamienia $x$ na $z$.

? Ile wynosi $\langle Z_0Z_1\rangle$ dla stanu Bella $|\Phi^+\rangle$?
+ 1
- 0
- −1
- 1/2
! Możliwe tylko wyniki 00 i 11 — zawsze parzysta liczba jedynek, więc $+1$.
%%%
---
id: qc-density
title: Macierz gęstości, szum i dekoherencja
summary: Stany mieszane, kanały kwantowe, T1/T2 i dlaczego NISQ jest „hałaśliwe”.
level: średni
---
### Macierz gęstości
Gdy nie znamy stanu dokładnie (albo badamy fragment splątanego układu), używamy **macierzy gęstości**:
$$\rho=\sum_ip_i|\psi_i\rangle\langle\psi_i|,\qquad \rho=\rho^\dagger,\quad \rho\succeq0,\quad \mathrm{Tr}\,\rho=1.$$
- stan czysty: $\rho=|\psi\rangle\langle\psi|$, **czystość** $\mathrm{Tr}\,\rho^2=1$,
- stan mieszany: $\mathrm{Tr}\,\rho^2<1$; dla kubitu $\rho=\frac12(I+\vec r\cdot\vec\sigma)$ z $|\vec r|<1$.

Wartość oczekiwana: $\langle O\rangle=\mathrm{Tr}(\rho O)$. Stan podukładu daje **ślad częściowy**: $\rho_A=\mathrm{Tr}_B\,\rho_{AB}$.

### Kanały kwantowe
Ogólna fizyczna operacja to kanał CPTP; w zapisie Krausa $\mathcal E(\rho)=\sum_kK_k\rho K_k^\dagger$, $\sum_kK_k^\dagger K_k=I$. Typowe modele szumu:
- **depolaryzacja**: $\mathcal E(\rho)=(1-p)\rho+p\,I/2$ — wektor Blocha się kurczy,
- **amplitude damping** (relaksacja, czas $T_1$): $|1\rangle$ spontanicznie przechodzi w $|0\rangle$,
- **dephasing** (czas $T_2$): zanika faza względna — kurczy się składowa $xy$ wektora Blocha,
- **błędy odczytu**: wynik 0 odczytany jako 1 i odwrotnie.

### Dekoherencja
Kubit oddziałuje z otoczeniem i informacja „przecieka” na zewnątrz — superpozycje zamieniają się w klasyczne mieszaniny. Czasy koherencji: $T_1$ (relaksacja energii) i $T_2\le2T_1$ (utrata fazy). Czas wykonania obwodu musi być znacznie krótszy niż $T_2$.

### Era NISQ
**NISQ** (*Noisy Intermediate‑Scale Quantum*, Preskill 2018): dziesiątki–tysiące kubitów fizycznych, bez pełnej korekcji błędów, błędy bramek dwukubitowych rzędu $10^{-3}$–$10^{-2}$. Wnioski dla QML: płytkie obwody, algorytmy hybrydowe, **mitygacja błędów** (np. *zero‑noise extrapolation* — celowe wzmocnienie szumu i ekstrapolacja do zera).

> [!warn] Szum a trenowanie
> Szum depolaryzujący sprawia, że $\langle O\rangle$ zbiega wykładniczo z głębokością do wartości dla stanu $I/2^n$ — to tzw. *noise‑induced barren plateaus*: gradienty znikają niezależnie od architektury.

@@@ fiszki
Q: Jakie warunki spełnia macierz gęstości?
A: Jest hermitowska, dodatnio półokreślona i ma ślad 1.

Q: Jak sprawdzić, czy stan jest czysty?
A: Czystość $\mathrm{Tr}\,\rho^2=1$ (dla stanu mieszanego $<1$).

Q: Jak obliczyć wartość oczekiwaną w formalizmie macierzy gęstości?
A: $\langle O\rangle=\mathrm{Tr}(\rho O)$.

Q: Co mierzą czasy $T_1$ i $T_2$?
A: $T_1$ — relaksację energii ($|1\rangle\to|0\rangle$), $T_2$ — utratę koherencji fazowej; $T_2\le2T_1$.

Q: Co oznacza skrót NISQ?
A: Noisy Intermediate‑Scale Quantum — zaszumione urządzenia średniej skali bez pełnej korekcji błędów.

Q: Na czym polega zero‑noise extrapolation?
A: Obwód uruchamia się przy kilku sztucznie zwiększonych poziomach szumu, a wynik ekstrapoluje do szumu zerowego.

@@@ quiz
? Kanał depolaryzujący z prawdopodobieństwem $p$ działa na wektor Blocha jak:
- obrót o kąt $p$
+ skurczenie o czynnik $(1-p)$
- odbicie względem równika
- przesunięcie w stronę $|1\rangle$
! $(1-p)\rho+pI/2$ odpowiada $\vec r\mapsto(1-p)\vec r$.

? Który proces opisuje czas $T_1$?
+ Relaksacja $|1\rangle\to|0\rangle$ (utrata energii)
- Utrata fazy względnej przy zachowaniu populacji
- Błąd odczytu
- Przesłuch między kubitami
! $T_1$ — amplitude damping; $T_2$ — dekoherencja fazowa.

? Czystość stanu $\rho=I/2$ wynosi:
- 1
+ 1/2
- 0
- 2
! $\mathrm{Tr}(I/2)^2=\mathrm{Tr}(I/4)=1/2$ — minimum dla kubitu.

? Dlaczego algorytmy NISQ preferują płytkie obwody?
- Bo głębokie obwody nie są unitarne
+ Bo błędy kumulują się z każdą bramką, a czas koherencji jest ograniczony
- Bo bramki dwukubitowe nie istnieją na sprzęcie
- Bo kompilator nie obsługuje więcej niż 10 warstw
! Każda bramka wnosi błąd rzędu $10^{-3}$–$10^{-2}$, a dekoherencja ogranicza czas działania.
%%%
---
id: qc-hardware
title: Uniwersalność, sprzęt i korekcja błędów
summary: Zbiory bramek uniwersalnych, transpilacja, platformy sprzętowe i kubity logiczne.
level: średni
---
### Uniwersalny zbiór bramek
Dowolną operację unitarną na $n$ kubitach można przybliżyć z dowolną dokładnością bramkami z małego zbioru, np. $\{H,T,\mathrm{CNOT}\}$ albo {dowolne bramki jednokubitowe + CNOT}. **Twierdzenie Solovaya–Kitaeva**: dokładność $\varepsilon$ wymaga tylko $O(\log^c(1/\varepsilon))$ bramek.

Bramki Clifforda ($H,S,\mathrm{CNOT}$) **nie** są uniwersalne — obwody złożone z samych Cliffordów symuluje się klasycznie efektywnie (**twierdzenie Gottesmana–Knilla**). Potrzebna jest bramka spoza tej grupy, np. $T$.

### Transpilacja
Kompilator (np. `transpile` w Qiskit) przepisuje obwód tak, by:
- używał **natywnych bramek** procesora (np. $\sqrt X$, $R_Z$, ECR/CZ),
- był zgodny z **łącznością** kubitów (*coupling map*) — gdy kubity nie sąsiadują, wstawia SWAP-y,
- był zoptymalizowany (mniej bramek dwukubitowych = mniej błędów).

### Platformy sprzętowe
| Platforma | Zalety | Wyzwania |
|---|---|---|
| Nadprzewodzące (IBM, Google) | szybkie bramki (ns), dojrzała produkcja | kriogenika ~10 mK, ograniczona łączność |
| Pułapki jonowe (Quantinuum, IonQ) | bardzo wysoka wierność, łączność „każdy z każdym” | wolniejsze bramki, skalowanie |
| Atomy neutralne (QuEra, Pasqal) | setki–tysiące kubitów, przestawialna geometria | wolniejsze cykle |
| Fotonika (Xanadu, PsiQuantum) | praca w wyższych temperaturach, sieci | straty, bramki probabilistyczne |
| Kubity spinowe (krzem) | potencjał integracji z CMOS | wczesny etap |

### Kwantowa korekcja błędów (QEC)
Informację jednego **kubitu logicznego** koduje się w wielu kubitach fizycznych; pomiary **syndromów** (parzystości) wykrywają błędy bez niszczenia zakodowanego stanu. **Kod powierzchniowy** (*surface code*) ma próg błędu około 1%: poniżej progu powiększanie kodu wykładniczo tłumi błędy logiczne (eksperymentalnie pokazane m.in. przez Google w 2024 r.). Koszt: setki–tysiące kubitów fizycznych na jeden logiczny. Algorytmy takie jak Shor wymagają maszyn **odpornych na błędy** (*fault‑tolerant*).

> [!key]
> - $\{H,T,\mathrm{CNOT}\}$ jest uniwersalny; same Cliffordy — nie (Gottesman–Knill).
> - NISQ → mitygacja błędów; FTQC → korekcja błędów i kubity logiczne.

@@@ fiszki
Q: Podaj przykład uniwersalnego zbioru bramek.
A: $\{H,T,\mathrm{CNOT}\}$ (albo dowolne bramki jednokubitowe + CNOT).

Q: Co mówi twierdzenie Gottesmana–Knilla?
A: Obwody złożone wyłącznie z bramek Clifforda ($H,S,\mathrm{CNOT}$), stanów bazowych i pomiarów Pauliego można efektywnie symulować klasycznie.

Q: Co robi transpilacja obwodu?
A: Przepisuje go na bramki natywne sprzętu, dopasowuje do łączności kubitów (wstawiając SWAP-y) i optymalizuje.

Q: Czym jest kubit logiczny?
A: Kubitem zakodowanym w wielu kubitach fizycznych za pomocą kodu korekcji błędów, chronionym przez pomiary syndromów.

Q: Jaki jest przybliżony próg błędu kodu powierzchniowego?
A: Około 1% błędu na operację fizyczną.

@@@ quiz
? Który zbiór bramek NIE jest uniwersalny?
- $\{H,T,\mathrm{CNOT}\}$
+ $\{H,S,\mathrm{CNOT}\}$
- wszystkie bramki jednokubitowe + CNOT
- $R_Y$, $R_Z$ z dowolnymi kątami + CZ
! $H,S,\mathrm{CNOT}$ generują grupę Clifforda — symulowalną klasycznie.

? Po co transpiler wstawia bramki SWAP?
- Aby zwiększyć splątanie
+ Aby wykonać bramkę dwukubitową na kubitach, które nie są fizycznie połączone
- Aby zmierzyć kubity w bazie X
- Aby zredukować szum odczytu
! Ograniczona łączność (coupling map) wymaga przesuwania stanów między kubitami.

? Która platforma ma zwykle łączność „każdy z każdym”, ale wolniejsze bramki?
- Nadprzewodząca
+ Pułapki jonowe
- Fotoniczna
- Kropki kwantowe
! Jony w pułapce oddziałują przez wspólne mody drgań.

? Co dzieje się poniżej progu kodu korekcji błędów, gdy zwiększamy rozmiar kodu?
+ Błąd logiczny maleje wykładniczo
- Błąd logiczny rośnie
- Nic się nie zmienia
- Kubity stają się klasyczne
! Na tym polega twierdzenie o progu (threshold theorem).
