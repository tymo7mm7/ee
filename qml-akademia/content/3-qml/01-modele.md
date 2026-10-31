---
id: qml-intro
title: Czym jest QML? Mapa dziedziny
summary: Cztery kombinacje danych i algorytmów, obietnice i realia kwantowego uczenia maszynowego.
level: podstawowy
---
**Kwantowe uczenie maszynowe** (QML, *Quantum Machine Learning*) bada, jak łączyć obliczenia kwantowe z uczeniem maszynowym. Najczęściej oznacza to modele, w których **obwód kwantowy jest częścią modelu** uczonego na danych.

### Taksonomia: dane × algorytm
| | algorytm klasyczny (C) | algorytm kwantowy (Q) |
|---|---|---|
| **dane klasyczne (C)** | CC — zwykłe ML (czasem inspirowane kwantowo, np. sieci tensorowe) | **CQ** — dane klasyczne kodowane w obwodzie (VQC, kernele kwantowe) — główny nurt |
| **dane kwantowe (Q)** | QC — klasyczne ML analizujące wyniki eksperymentów kwantowych (fazy materii, kalibracja, dekodery QEC) | QQ — kwantowe przetwarzanie stanów kwantowych |

### Dwie fale QML
1. **Algorytmy dla maszyn odpornych na błędy** (ok. 2009–2018): kwantowe SVM, regresja, PCA, systemy rekomendacyjne oparte na HHL i QRAM — obiecywały wykładnicze przyspieszenia, ale wymagały QRAM i wiele z nich **zdekwantyzowano**.
2. **Modele wariacyjne / NISQ** (od ok. 2018): parametryzowane obwody trenowane hybrydowo (VQC, QNN) i kernele kwantowe. Działają na dzisiejszym sprzęcie, lecz przewaga jest niepewna.

### Model QML w jednym wzorze
$$f_{\boldsymbol\theta}(\mathbf x)=\langle 0|\,U^\dagger(\mathbf x,\boldsymbol\theta)\,M\,U(\mathbf x,\boldsymbol\theta)\,|0\rangle$$
Dane $\mathbf x$ i parametry $\boldsymbol\theta$ sterują bramkami; wynikiem jest wartość oczekiwana obserwabli $M$. Uczenie to klasyczna optymalizacja $\boldsymbol\theta$.

### Co może dać QML?
- **Ekspresywność**: przestrzeń cech o wymiarze rosnącym wykładniczo z liczbą kubitów, korelacje kwantowe.
- **Przewagę dla danych kwantowych**: tu jest ona udowodniona (np. uczenie z pamięcią kwantową — Huang i in., *Science* 2022).
- **Konkretne problemy ze strukturą**: istnieją sztuczne zadania (np. oparte na logarytmie dyskretnym, Liu i in. 2021), w których kernele kwantowe mają dowiedlną przewagę.

### Realia
- Większość demonstracji na danych klasycznych nie przebija dobrych modeli klasycznych.
- Problemy z trenowalnością (barren plateaus), szumem i kosztem pomiarów.
- Modele, które łatwo trenować, często da się **efektywnie symulować klasycznie**.

> [!key]
> QML to dziś głównie **CQ z modelami wariacyjnymi i kernelami**. Kluczowe pytania: jak kodować dane, jak trenować (gradienty), czy model jest trenowalny i czy daje przewagę.

@@@ fiszki
Q: Co oznaczają skróty CC, CQ, QC, QQ w taksonomii QML?
A: Pierwsza litera to rodzaj danych (klasyczne/kwantowe), druga — rodzaj algorytmu. CQ = dane klasyczne przetwarzane kwantowo (główny nurt).

Q: Zapisz ogólną postać modelu wariacyjnego QML.
A: $f_{\boldsymbol\theta}(\mathbf x)=\langle0|U^\dagger(\mathbf x,\boldsymbol\theta)MU(\mathbf x,\boldsymbol\theta)|0\rangle$ — wartość oczekiwana obserwabli po obwodzie zależnym od danych i parametrów.

Q: Czym różnią się dwie „fale” QML?
A: Pierwsza: algorytmy dla maszyn odpornych na błędy oparte na HHL/QRAM (wiele zdekwantyzowano). Druga: hybrydowe modele wariacyjne i kernele na sprzęcie NISQ.

Q: Gdzie przewaga kwantowa w uczeniu jest najlepiej uzasadniona?
A: Przy danych kwantowych (uczenie z eksperymentów kwantowych, pamięć kwantowa) oraz w specjalnie skonstruowanych problemach (np. opartych na logarytmie dyskretnym).

Q: Podaj przykład zadania typu QC.
A: Klasyczna sieć neuronowa klasyfikująca fazy materii na podstawie pomiarów stanów kwantowych albo dekodująca syndromy kodu korekcji błędów.

@@@ quiz
? Wariacyjny klasyfikator kwantowy uczony na obrazach MNIST to przykład kategorii:
- CC
+ CQ
- QC
- QQ
! Dane są klasyczne, a model (algorytm) — kwantowy.

? Co było głównym problemem pierwszej fali QML opartej na HHL?
+ Wymaganie QRAM i założenia, które umożliwiły klasyczną dekwantyzację
- Zbyt mała liczba parametrów
- Brak bramek dwukubitowych
- Zbyt płytkie obwody
! Algorytmy wymagały efektywnego ładowania danych; przy analogicznych założeniach klasyczne algorytmy próbkujące okazały się tylko wielomianowo wolniejsze.

? Jaką wielkość zwraca typowy model wariacyjny QML?
- Cały wektor stanu $|\psi\rangle$
+ Wartość oczekiwaną obserwabli (np. $\langle Z\rangle$)
- Macierz unitarną obwodu
- Liczbę bramek
! Z pomiarów estymujemy $\langle M\rangle$, które następnie przetwarzamy (np. na prawdopodobieństwo klasy).

? Gdzie przewaga kwantowa w uczeniu ma najsilniejsze dotychczasowe uzasadnienie?
- W klasyfikacji obrazów
- W przetwarzaniu języka naturalnego
+ W uczeniu z danych kwantowych (stanów i procesów kwantowych)
- W rekomendacjach produktów
! Wyniki teoretyczne i eksperymentalne (Huang i in. 2021–2022) pokazują wykładniczą oszczędność liczby eksperymentów.
%%%
---
id: qml-encoding
title: Kodowanie danych klasycznych
summary: Kodowanie bazowe, amplitudowe, kątowe i IQP — kompromisy między kubitami, głębokością i ekspresywnością.
level: średni
---
Zanim obwód coś „policzy”, dane $\mathbf x\in\mathbb R^d$ muszą trafić do stanu kwantowego: $\mathbf x\mapsto|\phi(\mathbf x)\rangle=S(\mathbf x)|0\rangle$. Wybór kodowania (*encoding*, *embedding*) w dużej mierze **określa, jakie funkcje model może wyrazić**.

### 1. Kodowanie bazowe (basis encoding)
Ciąg bitów → stan bazowy: $(1,0,1)\mapsto|101\rangle$. Liczby rzeczywiste trzeba zdyskretyzować ($k$ bitów na cechę → $kd$ kubitów). Proste, ale kosztowne w kubitach; używane w algorytmach arytmetycznych.

### 2. Kodowanie amplitudowe (amplitude encoding)
$$\mathbf x\mapsto|\mathbf x\rangle=\frac{1}{\|\mathbf x\|}\sum_{i=0}^{N-1}x_i|i\rangle$$
- $N$ cech w **$\lceil\log_2N\rceil$ kubitach** — wykładniczo zwięźle (1024 cechy → 10 kubitów),
- ale przygotowanie dowolnego stanu wymaga w ogólności **$O(N)$ bramek** (głębokość!) albo QRAM,
- tracimy normę $\|\mathbf x\|$ (można ją dołączyć jako dodatkową cechę).

### 3. Kodowanie kątowe (angle encoding)
Każda cecha to kąt rotacji na osobnym kubicie:
$$|\phi(\mathbf x)\rangle=\bigotimes_{j=1}^{d}R_Y(x_j)|0\rangle=\bigotimes_j\big(\cos\tfrac{x_j}{2}|0\rangle+\sin\tfrac{x_j}{2}|1\rangle\big)$$
- $d$ kubitów, głębokość 1 — idealne dla NISQ,
- stan produktowy (bez splątania); cechy warto przeskalować, np. do $[0,\pi]$,
- **dense angle encoding**: dwie cechy na kubit, np. $R_Z(x_2)R_Y(x_1)$.

### 4. Kodowanie IQP / ZZ
$$|\phi(\mathbf x)\rangle=U_Z(\mathbf x)H^{\otimes n}U_Z(\mathbf x)H^{\otimes n}|0\rangle,\qquad U_Z(\mathbf x)=\exp\Big(i\sum_jx_jZ_j+i\sum_{j<k}x_jx_kZ_jZ_k\Big)$$
Splątujące, z członami iloczynowymi $x_jx_k$; przypuszczalnie trudne do symulacji klasycznej (obwody IQP). W Qiskit podobny wariant to `ZZFeatureMap`.

### 5. Kodowanie hamiltonianowe
$\mathbf x$ steruje ewolucją $e^{-iH(\mathbf x)t}$ — naturalne dla danych fizycznych (czas, pole, parametry układu).

### Porównanie
| Kodowanie | Kubity | Głębokość | Uwagi |
|---|---|---|---|
| bazowe | $\sim kd$ | $O(1)$ | dane binarne |
| amplitudowe | $\log_2 d$ | $O(d)$ | zwięzłe, drogie w przygotowaniu |
| kątowe | $d$ | $O(1)$ | proste, bez splątania |
| IQP / ZZ | $d$ | $O(d^2)$ dla wszystkich par | splątanie, iloczyny cech |

> [!key] Kodowanie definiuje model
> Dla modelu $f(\mathbf x)=\langle\phi(\mathbf x)|M|\phi(\mathbf x)\rangle$ kodowanie wyznacza przestrzeń cech. Przy kodowaniu kątowym $f$ jest **szeregiem Fouriera** w $\mathbf x$ z częstotliwościami $\{-1,0,1\}$ na cechę. Wyższe częstotliwości daje wielokrotne kodowanie (*data re‑uploading*).

> [!warn] Wąskie gardło danych
> Ładowanie dużych zbiorów danych to „data loading problem”: obiecane wykładnicze przyspieszenia znikają, jeśli samo wczytanie danych kosztuje $O(N)$.

@@@ fiszki
Q: Ile kubitów potrzebuje kodowanie amplitudowe $N$ cech?
A: $\lceil\log_2N\rceil$ kubitów (ale przygotowanie stanu wymaga zwykle $O(N)$ bramek).

Q: Zapisz kodowanie kątowe z rotacjami $R_Y$.
A: $|\phi(\mathbf x)\rangle=\bigotimes_jR_Y(x_j)|0\rangle=\bigotimes_j(\cos\frac{x_j}{2}|0\rangle+\sin\frac{x_j}{2}|1\rangle)$.

Q: Jaka jest główna wada kodowania amplitudowego?
A: Koszt przygotowania stanu: dowolny stan $n$ kubitów wymaga w ogólności $O(2^n)$ bramek (albo QRAM); traci się też normę wektora.

Q: Czym jest kodowanie IQP/ZZ?
A: Kodowaniem z warstwami Hadamarda i diagonalnymi bramkami $e^{i(\sum x_jZ_j+\sum x_jx_kZ_jZ_k)}$ — splątującym, z iloczynami cech (w Qiskit: ZZFeatureMap).

Q: Na czym polega dense angle encoding?
A: Na kodowaniu dwóch cech na jednym kubicie, np. przez $R_Y(x_1)$ i $R_Z(x_2)$.

Q: Dlaczego kodowanie jest tak ważne w QML?
A: Bo wyznacza przestrzeń cech i klasę funkcji, które model może wyrazić (m.in. spektrum Fouriera modelu).

Q: Czym jest problem ładowania danych (data loading problem)?
A: Wczytanie $N$ liczb do stanu kwantowego kosztuje zwykle $O(N)$, co może zniwelować teoretyczne przyspieszenia algorytmów.

@@@ quiz
? Masz 256 cech. Ile kubitów wymaga kodowanie amplitudowe?
- 256
- 16
+ 8
- 128
! $\log_2256=8$.

? Które kodowanie daje stan produktowy (bez splątania)?
+ Kątowe $\bigotimes_jR_Y(x_j)|0\rangle$
- ZZ feature map
- IQP
- Amplitudowe dla dowolnego $\mathbf x$
! Każdy kubit niezależnie koduje jedną cechę.

? Dlaczego cechy w kodowaniu kątowym często skaluje się np. do $[0,\pi]$?
- Bo bramki nie przyjmują ujemnych kątów
+ Bo rotacje są okresowe — zbyt duży zakres sprawia, że różne wartości trafiają w podobne stany
- Aby zmniejszyć liczbę kubitów
- Bo wymaga tego twierdzenie o nieklonowaniu
! Prawdopodobieństwa po $R_Y(x)$ mają okres $2\pi$; skalowanie zachowuje rozróżnialność danych.

? Jaki stan daje kodowanie amplitudowe wektora $\mathbf x=(3,4)$?
- $3|0\rangle+4|1\rangle$
+ $0.6|0\rangle+0.8|1\rangle$
- $\frac{1}{\sqrt2}(|0\rangle+|1\rangle)$
- $0.75|0\rangle+|1\rangle$
! Normalizacja: $\|\mathbf x\|=5$.

? Jaka wada kodowania bazowego dominuje dla danych rzeczywistych?
+ Duża liczba kubitów potrzebna do dyskretyzacji
- Bardzo duża głębokość obwodu
- Brak możliwości pomiaru
- Konieczność użycia QRAM
! $k$ bitów precyzji na każdą z $d$ cech daje $kd$ kubitów.
%%%
---
id: qml-featuremaps
title: Kwantowe mapy cech i przestrzeń Hilberta
summary: Obwód kodujący jako mapa cech; związek z metodami jądrowymi i modelami liniowymi.
level: średni
lab: kernel
---
### Mapa cech
W klasycznym ML **mapa cech** $\varphi:\mathcal X\to\mathcal F$ przenosi dane do (często wysokowymiarowej) przestrzeni, w której problem staje się liniowo separowalny. W QML mapą cech jest obwód kodujący:
$$\mathbf x\ \mapsto\ \rho(\mathbf x)=|\phi(\mathbf x)\rangle\langle\phi(\mathbf x)|,$$
a przestrzenią cech — przestrzeń macierzy gęstości (wymiar $4^n$) z iloczynem skalarnym Hilberta–Schmidta $\mathrm{Tr}(\rho\sigma)$.

### Modele kwantowe są liniowe w przestrzeni cech
Model z pomiarem obserwabli $M$:
$$f(\mathbf x)=\mathrm{Tr}\big(M\rho(\mathbf x)\big)$$
jest **funkcją liniową** cech $\rho(\mathbf x)$ — dokładnie jak SVM czy regresja liniowa na cechach $\varphi(\mathbf x)$. Trenowalny obwód po kodowaniu, $W(\boldsymbol\theta)$, to po prostu wybór obserwabli $M_{\boldsymbol\theta}=W^\dagger MW$ (Schuld & Killoran 2019; Schuld 2021, „Supervised quantum machine learning models are kernel methods”).

### Kernel indukowany przez mapę
$$k(\mathbf x,\mathbf x')=\mathrm{Tr}\big(\rho(\mathbf x)\rho(\mathbf x')\big)=|\langle\phi(\mathbf x)|\phi(\mathbf x')\rangle|^2$$
Z **twierdzenia o reprezentantach**: optymalny model (przy stracie z regularyzacją) ma postać $f(\mathbf x)=\sum_i\alpha_ik(\mathbf x_i,\mathbf x)$ — zamiast optymalizować obwód, wystarczy policzyć kernel na danych treningowych.

### ZZ feature map (Havlíček i in., *Nature* 2019)
Dla 2 cech (powtarzane $r$ razy):
$$U_\Phi(\mathbf x)=\exp\Big(i\big[\phi_1(\mathbf x)Z_1+\phi_2(\mathbf x)Z_2+\phi_{12}(\mathbf x)Z_1Z_2\big]\Big)H^{\otimes2},\qquad \phi_j=x_j,\quad \phi_{12}=(\pi-x_1)(\pi-x_2).$$
Autorzy argumentowali, że dla wielu kubitów taki kernel jest trudny do obliczenia klasycznie (obwody typu IQP).

### Dobre mapy cech
- Powinny pasować do **struktury danych** (symetrie, okresowość) — inaczej kernel kwantowy przegrywa z RBF.
- Muszą unikać **koncentracji**: zbyt „losowe” mapy dają $k(\mathbf x,\mathbf x')\approx2^{-n}$ dla wszystkich par.
- Można je **uczyć** (trainable feature maps, *kernel target alignment*).

> [!tip] Zobacz w Lab
> „Kernele kwantowe” pokazują macierz Grama dla mapy kątowej i ZZ oraz wpływ skali wejścia.

@@@ fiszki
Q: Czym jest kwantowa mapa cech?
A: Odwzorowaniem $\mathbf x\mapsto\rho(\mathbf x)=|\phi(\mathbf x)\rangle\langle\phi(\mathbf x)|$ realizowanym przez obwód kodujący; przestrzenią cech jest przestrzeń operatorów.

Q: Dlaczego kwantowe modele z pomiarem są liniowe w przestrzeni cech?
A: Bo $f(\mathbf x)=\mathrm{Tr}(M\rho(\mathbf x))$ jest iloczynem skalarnym Hilberta–Schmidta obserwabli $M$ i cech $\rho(\mathbf x)$.

Q: Jaki kernel indukuje kwantowa mapa cech?
A: $k(\mathbf x,\mathbf x')=\mathrm{Tr}(\rho(\mathbf x)\rho(\mathbf x'))=|\langle\phi(\mathbf x)|\phi(\mathbf x')\rangle|^2$ (wierność stanów).

Q: Co mówi twierdzenie o reprezentantach w kontekście QML?
A: Optymalny model minimalizujący regularyzowaną stratę ma postać $\sum_i\alpha_ik(\mathbf x_i,\mathbf x)$ — wystarczy znać kernel na danych treningowych.

Q: Kto zaproponował ZZ feature map i kernele kwantowe na sprzęcie?
A: Havlíček i in. (IBM), *Nature* 2019 — „Supervised learning with quantum‑enhanced feature spaces”.

Q: Jak trenowalny obwód po kodowaniu wpływa na model w ujęciu jądrowym?
A: Wybiera obserwablę $M_{\boldsymbol\theta}=W^\dagger(\boldsymbol\theta)MW(\boldsymbol\theta)$ — model pozostaje liniowy w tych samych cechach.

@@@ quiz
? W ujęciu Schuld (2021) nadzorowane modele kwantowe są w istocie:
- sieciami rekurencyjnymi
+ metodami jądrowymi (modelami liniowymi w przestrzeni cech)
- drzewami decyzyjnymi
- modelami generatywnymi
! $f(\mathbf x)=\mathrm{Tr}(M\rho(\mathbf x))$ jest liniowa w $\rho(\mathbf x)$.

? Jaki jest wymiar przestrzeni cech (macierzy gęstości) dla $n$ kubitów?
- $n$
- $2n$
- $2^n$
+ $4^n$
! Macierze $2^n\times2^n$ mają $4^n$ elementów.

? Co oznacza koncentracja kernela kwantowego?
+ Wartości $k(\mathbf x,\mathbf x')$ dla różnych par są prawie takie same (np. $\approx2^{-n}$), więc przy skończonej liczbie pomiarów trudno je rozróżnić
- Kernel jest zawsze równy 1
- Kernel nie jest symetryczny
- Kernel wymaga QRAM
! Do odróżnienia tak małych różnic potrzeba wykładniczo wielu shotów.
%%%
---
id: qml-kernels
title: Kernele kwantowe i QSVM
summary: Estymacja macierzy Grama na komputerze kwantowym, SVM z kernelem kwantowym, koncentracja i szerokość pasma.
level: średni
lab: kernel
---
### Przepis na QSVM
1. Wybierz mapę cech $U(\mathbf x)$.
2. Dla każdej pary danych treningowych oszacuj $K_{ij}=|\langle\phi(\mathbf x_i)|\phi(\mathbf x_j)\rangle|^2$ **na komputerze kwantowym**.
3. Wytrenuj **klasyczny** SVM z kernelem `precomputed` na macierzy $K$.
4. Predykcja: policz $k(\mathbf x,\mathbf x_i)$ dla wektorów nośnych.

Optymalizacja jest **wypukła** (problem dualny SVM) — trening nie ma barren plateaus, a dla danego kernela optimum jest globalne.

### Jak zmierzyć kernel?
- **Compute–uncompute (test odwrócenia)**: obwód $U^\dagger(\mathbf x_j)U(\mathbf x_i)|0\rangle$; $K_{ij}=p(0\dots0)$. Wymaga $n$ kubitów i podwójnej głębokości.
- **Swap test**: dwa rejestry + kubit pomocniczy; $p(0)=\frac12+\frac12|\langle\phi_i|\phi_j\rangle|^2$. Wymaga $2n+1$ kubitów.

### Koszt
Macierz Grama to $O(m^2)$ obwodów dla $m$ próbek treningowych, każdy po $S$ shotów. Dla $m=10^4$ to ok. $5\cdot10^7$ obwodów — poważne ograniczenie skalowania.

### Koncentracja wykładnicza
Dla wielu kubitów i „ekspresywnych” map cech kernel się koncentruje: $k(\mathbf x,\mathbf x')\approx\mathbb E[k]$, a wariancja maleje wykładniczo z $n$ (Thanasilp i in. 2022). Przyczyny: ekspresywność, splątanie (globalne pomiary), szum. Skutek: przy wielomianowej liczbie shotów macierz Grama to szum, a model **nie generalizuje**.

**Rozwiązania:**
- **szerokość pasma** (*bandwidth*): przeskaluj wejście $\mathbf x\to s\mathbf x$ z małym $s$ (Shaydulin & Wild 2022; Canatar i in. 2022),
- **projected quantum kernels** (Huang i in. 2021): porównuj zredukowane stany jednokubitowe, np. $k=\exp\big(-\gamma\sum_j\|\rho_j(\mathbf x)-\rho_j(\mathbf x')\|_F^2\big)$,
- dopasowanie kernela do danych (*kernel target alignment*).

### Kiedy kernel kwantowy ma sens?
Gdy dane mają strukturę, której kernel klasyczny łatwo nie uchwyci. Liu, Arunachalam i Temme (2021) skonstruowali problem oparty na logarytmie dyskretnym, w którym kernel kwantowy ma **dowiedlną** przewagę (przy założeniach kryptograficznych). Dla typowych danych tabelarycznych dobrze dobrany RBF zwykle wystarcza.

```python
# Qiskit Machine Learning
from qiskit.circuit.library import ZZFeatureMap
from qiskit_machine_learning.kernels import FidelityQuantumKernel
from sklearn.svm import SVC

fmap = ZZFeatureMap(feature_dimension=2, reps=2)
qkernel = FidelityQuantumKernel(feature_map=fmap)

K_train = qkernel.evaluate(x_vec=X_train)
K_test = qkernel.evaluate(x_vec=X_test, y_vec=X_train)
svc = SVC(kernel="precomputed").fit(K_train, y_train)
print(svc.score(K_test, y_test))
```

@@@ fiszki
Q: Jak wygląda trening QSVM?
A: Wartości kernela $K_{ij}$ estymuje komputer kwantowy, a SVM (problem wypukły) trenuje się klasycznie na macierzy Grama.

Q: Na czym polega metoda compute–uncompute estymacji kernela?
A: Uruchamiamy $U^\dagger(\mathbf x_j)U(\mathbf x_i)|0\rangle$ i mierzymy; $K_{ij}$ to prawdopodobieństwo wyniku $0\dots0$.

Q: Ile kubitów wymaga swap test dla stanów $n$‑kubitowych?
A: $2n+1$ (dwa rejestry i kubit pomocniczy); $p(0)=\frac12+\frac12|\langle\phi|\psi\rangle|^2$.

Q: Ile obwodów potrzeba do macierzy Grama dla $m$ próbek?
A: $O(m^2)$ — dokładnie $m(m-1)/2$ dla symetrycznej macierzy z jedynkami na przekątnej; każdy obwód z wieloma shotami.

Q: Czym jest wykładnicza koncentracja kerneli kwantowych?
A: Wartości kernela dla różnych par skupiają się wokół stałej z wariancją malejącą wykładniczo z liczbą kubitów — przy skończonej liczbie shotów kernel staje się nieinformatywny.

Q: Jak szerokość pasma pomaga kernelom kwantowym?
A: Skalowanie wejścia $\mathbf x\to s\mathbf x$ z małym $s$ zmniejsza „rozrzut” stanów, zapobiega koncentracji i poprawia generalizację.

Q: Czym jest projected quantum kernel?
A: Kernelem porównującym zredukowane stany (np. jednokubitowe) zamiast pełnych stanów, np. $\exp(-\gamma\sum_j\|\rho_j(\mathbf x)-\rho_j(\mathbf x')\|^2)$ — mniej podatnym na koncentrację.

@@@ quiz
? Która część QSVM jest wykonywana na komputerze kwantowym?
+ Estymacja wartości kernela $k(\mathbf x_i,\mathbf x_j)$
- Rozwiązanie problemu dualnego SVM
- Wybór hiperparametru C
- Obliczenie dokładności
! Optymalizacja SVM jest klasyczna i wypukła.

? Dlaczego trening QSVM nie cierpi na barren plateaus?
- Bo używa tylko jednego kubitu
+ Bo optymalizacja jest wypukła i nie dotyczy parametrów obwodu
- Bo kernel jest zawsze równy 1
- Bo nie używa pomiarów
! Kernel może jednak cierpieć na analogiczny problem — koncentrację wartości.

? W swap teście otrzymano $p(0)=0.75$. Ile wynosi $|\langle\phi|\psi\rangle|^2$?
- 0.75
+ 0.5
- 0.25
- 1
! $p(0)=\frac12+\frac12F$, więc $F=2\cdot0.75-1=0.5$.

? Jaki efekt ma zbyt duża skala wejścia (szerokość pasma) w kernelu kwantowym?
+ Macierz Grama zbliża się do jednostkowej — model zapamiętuje dane i słabo generalizuje
- Macierz Grama staje się macierzą samych jedynek
- Kernel przestaje być symetryczny
- Trening przestaje być wypukły
! Stany różnych punktów stają się prawie ortogonalne. Sprawdź w Lab „Kernele kwantowe”.

? Ile obwodów (bez liczenia shotów) trzeba wykonać dla symetrycznej macierzy Grama ze 100 próbek?
- 100
+ 4950
- 10 000
- 200
! $m(m-1)/2=4950$ (na przekątnej są jedynki).
%%%
---
id: qml-pqc
title: Parametryzowane obwody kwantowe (ansatz)
summary: Warstwy rotacji i splątania, hardware‑efficient ansatz, ekspresywność i inicjalizacja.
level: średni
---
**Parametryzowany obwód kwantowy** (PQC, *variational circuit*, *ansatz*) to obwód $W(\boldsymbol\theta)$ z bramkami o trenowalnych kątach. W QML pełni rolę „warstw” sieci neuronowej.

### Typowa budowa warstwy
1. **Rotacje jednokubitowe** na każdym kubicie, np. $R_Z(\theta_1)R_Y(\theta_2)R_Z(\theta_3)$ (dowolna bramka SU(2)).
2. **Blok splątujący**: CNOT/CZ w łańcuchu, pierścieniu albo „wszystkie ze wszystkimi”.
3. Powtórz $L$ razy.

Przykładowe szablony:
- PennyLane: `BasicEntanglerLayers`, `StronglyEntanglingLayers` (rotacje `Rot` + CNOT-y o rosnącym zasięgu),
- Qiskit: `RealAmplitudes` ($R_Y$ + CNOT — amplitudy rzeczywiste), `EfficientSU2` ($R_Y$, $R_Z$ + CNOT), `TwoLocal`.

### Hardware‑efficient vs problem‑inspired
- **Hardware‑efficient ansatz** (HEA): bramki natywne, łączność sprzętu, małe głębokości — ale „ogólny” i podatny na barren plateaus przy wielu warstwach.
- **Problem‑inspired**: uwzględnia strukturę zadania (UCC w chemii, QAOA, **ansatze ekwiwariantne** respektujące symetrie danych — tzw. geometric QML).

### Ekspresywność
Jak duży fragment przestrzeni stanów obwód osiąga? Miara Sima i in. (2019): odległość rozkładu wierności $F=|\langle\psi_{\boldsymbol\theta}|\psi_{\boldsymbol\phi}\rangle|^2$ dla losowych parametrów od rozkładu dla stanów losowych Haara. **Zdolność splątywania** (*entangling capability*) to średnia miara splątania generowanych stanów (np. Meyera–Wallacha).

> [!warn] Paradoks ekspresywności
> Im bardziej ekspresywny obwód (bliższy 2‑designowi), tym **mniejsze gradienty** — Holmes i in. (2022) powiązali ekspresywność z barren plateaus. Dobry ansatz jest „wystarczająco” ekspresywny dla zadania, ale nie bardziej.

### Liczba parametrów a moc
Dla $n$ kubitów i $L$ warstw z rotacjami Eulera mamy $3nL$ parametrów. Więcej parametrów nie zawsze oznacza większą moc: obwód ma **wymiar efektywny** ograniczony wymiarem rozmaitości osiągalnych stanów. *Nadparametryzacja* (Larocca i in. 2023) — gdy parametrów jest dość, krajobraz straty traci złe minima lokalne.

### Inicjalizacja
- losowa z $[0,2\pi)$ — dla głębokich obwodów prowadzi do barren plateaus,
- **identity‑block initialization** (Grant i in. 2019): bloki $WW^\dagger$, tak by obwód startował od identyczności,
- małe kąty (blisko zera), inicjalizacja warstwa po warstwie, *warm start*.

@@@ fiszki
Q: Z czego składa się typowa warstwa hardware‑efficient ansatz?
A: Z rotacji jednokubitowych na każdym kubicie (np. $R_Y$, $R_Z$) i bloku bramek splątujących (CNOT/CZ) zgodnego z łącznością sprzętu.

Q: Czym jest ekspresywność PQC?
A: Zdolnością obwodu do generowania szerokiej klasy stanów; mierzy się ją np. odległością rozkładu wierności od rozkładu Haara.

Q: Jaki jest związek ekspresywności z gradientami?
A: Im bliżej rozkładu Haara (2‑designu), tym mniejsza wariancja gradientów — barren plateaus (Holmes i in. 2022).

Q: Czym różni się RealAmplitudes od EfficientSU2 w Qiskit?
A: RealAmplitudes używa tylko $R_Y$ i CNOT (amplitudy rzeczywiste), EfficientSU2 — $R_Y$ i $R_Z$ (amplitudy zespolone).

Q: Czym są ansatze ekwiwariantne (geometric QML)?
A: Obwodami zaprojektowanymi tak, by respektowały symetrie danych (np. permutacje, obroty) — mniejsza przestrzeń hipotez, lepsza trenowalność i generalizacja.

Q: Na czym polega identity‑block initialization?
A: Na inicjalizacji bloków jako $W(\boldsymbol\theta)W^\dagger(\boldsymbol\theta)$, tak by obwód startował od identyczności i miał duże gradienty.

@@@ quiz
? Ile parametrów ma ansatz z $n=4$ kubitami, $L=3$ warstwami i rotacją Eulera (3 kąty) na każdym kubicie w każdej warstwie?
- 12
+ 36
- 24
- 81
! $3\cdot n\cdot L=3\cdot4\cdot3=36$.

? Co jest typową wadą hardware‑efficient ansatz przy wielu warstwach?
+ Barren plateaus (zanikające gradienty)
- Zbyt mała liczba parametrów
- Niezgodność z łącznością sprzętu
- Brak splątania
! Ogólny, głęboki HEA upodabnia się do losowego operatora unitarnego (2‑designu).

? Obwód wariacyjny na $n$ kubitach bez bramek splątujących:
- jest uniwersalny
+ rozkłada się na $n$ niezależnych kubitów i jest łatwy do symulacji klasycznej
- nie daje się zmierzyć
- zawsze ma barren plateaus
! Stan pozostaje produktowy.

? Szablon `StronglyEntanglingLayers` pochodzi z biblioteki:
+ PennyLane
- Qiskit
- Cirq
- scikit-learn
! W Qiskit odpowiednikami są np. `EfficientSU2` i `TwoLocal`.
%%%
---
id: qml-vqc
title: Wariacyjny klasyfikator kwantowy (VQC/QNN)
summary: Pełny pipeline: kodowanie → ansatz → pomiar → strata → optymalizacja hybrydowa.
level: średni
lab: train
---
### Pipeline
1. **Przygotowanie danych**: standaryzacja, ewentualnie PCA do $n$ cech, skalowanie do zakresu kątów.
2. **Kodowanie** $S(\mathbf x)$ (np. kątowe).
3. **Ansatz** $W(\boldsymbol\theta)$ (warstwy rotacji i splątania).
4. **Pomiar** obserwabli: $\langle Z_0\rangle$, suma $\sum_j\langle Z_j\rangle$, parzystość — albo próbki bitstringów.
5. **Post‑processing**: zamiana na prawdopodobieństwo lub etykietę, np. $p(y{=}1)=\frac{1-\langle Z_0\rangle}{2}$, ewentualnie skalowanie $w\langle Z\rangle+b$.
6. **Strata**: entropia krzyżowa, MSE, hinge.
7. **Optymalizacja**: gradient (parameter‑shift lub adjoint) + Adam, albo SPSA, COBYLA…

$$\mathcal L(\boldsymbol\theta)=\frac{1}{m}\sum_{i=1}^{m}\ell\big(f_{\boldsymbol\theta}(\mathbf x_i),y_i\big)$$

### Wiele klas
- odczyt kilku kubitów: $\big(\langle Z_0\rangle,\dots,\langle Z_{K-1}\rangle\big)\to$ softmax,
- interpretacja bitstringów: wynik pomiaru $b$ mapowany na klasę (np. parzystość, $b\bmod K$) — tak działają `SamplerQNN` i `VQC` w Qiskit.

### Modele hybrydowe
Kwantowa „warstwa” w sieci klasycznej (PyTorch/TensorFlow): `qml.qnn.TorchLayer` w PennyLane, `TorchConnector` w Qiskit. Np. klasyczna sieć konwolucyjna redukuje obraz do 4 cech → obwód 4‑kubitowy → warstwa liniowa. Uwaga: w takiej architekturze trudno ustalić, czy obwód cokolwiek wnosi — porównaj z wersją bez części kwantowej.

### Koszt treningu
Na epokę: $m$ próbek × $(1+2P)$ obwodów dla parameter‑shift × $S$ shotów. Dla $m=1000$, $P=50$, $S=1000$ to ok. $10^8$ uruchomień obwodu na epokę. Dlatego eksperymenty QML na sprzęcie dotyczą małych zbiorów danych.

### Dobre praktyki
- Zawsze porównuj z **klasyczną bazą** o podobnej liczbie parametrów (regresja logistyczna, MLP, SVM z RBF).
- Raportuj wyniki na zbiorze testowym, dla wielu ziaren losowych.
- Sprawdzaj wariancję gradientów na początku treningu.
- Testuj z realistycznym szumem (symulator z modelem szumu i skończoną liczbą shotów).

```python
import pennylane as qml
from pennylane import numpy as np

n = 2
dev = qml.device("default.qubit", wires=n)

@qml.qnode(dev)
def circuit(weights, x):
    qml.AngleEmbedding(x, wires=range(n), rotation="Y")
    qml.StronglyEntanglingLayers(weights, wires=range(n))
    return qml.expval(qml.PauliZ(0))

def cost(weights, X, Y):
    preds = qml.math.stack([circuit(weights, x) for x in X])  # wartości z [-1, 1]
    return np.mean((preds - Y) ** 2)                           # Y w {-1, +1}

shape = qml.StronglyEntanglingLayers.shape(n_layers=3, n_wires=n)
weights = np.random.uniform(0, 2 * np.pi, size=shape, requires_grad=True)
opt = qml.AdamOptimizer(0.1)
for epoch in range(50):
    weights = opt.step(lambda w: cost(w, X, Y), weights)
```

> [!tip] Lab
> W „VQC vs klasyczne ML” zobaczysz trening na żywo, liczbę obwodów na epokę i porównanie z regresją logistyczną oraz MLP.

@@@ fiszki
Q: Wymień etapy pipeline'u wariacyjnego klasyfikatora kwantowego.
A: Preprocessing → kodowanie $S(\mathbf x)$ → ansatz $W(\boldsymbol\theta)$ → pomiar → post‑processing → strata → klasyczna optymalizacja (w pętli).

Q: Jak zamienić $\langle Z\rangle$ na prawdopodobieństwo klasy?
A: Np. $p(y{=}1)=\frac{1-\langle Z\rangle}{2}$ (albo $\sigma(w\langle Z\rangle+b)$).

Q: Ile obwodów na próbkę wymaga jeden krok gradientu parameter‑shift dla $P$ parametrów?
A: $2P$ obwodów (plus 1 na samą wartość funkcji), każdy z wieloma shotami.

Q: Jak realizuje się klasyfikację wieloklasową w VQC?
A: Przez odczyt kilku obserwabli i softmax albo przez mapowanie bitstringów z pomiaru na klasy (np. parzystość, modulo K).

Q: Dlaczego zawsze trzeba porównywać VQC z klasyczną bazą?
A: Bo dobry wynik VQC nie oznacza przewagi — prosty model klasyczny o podobnej liczbie parametrów często działa równie dobrze lub lepiej.

Q: Czym jest model hybrydowy kwantowo‑klasyczny?
A: Siecią, w której obwód kwantowy jest jedną z warstw (np. TorchLayer w PennyLane, TorchConnector w Qiskit), trenowaną end‑to‑end.

@@@ quiz
? Model zwraca $\langle Z_0\rangle=-0.6$. Ile wynosi $p(y{=}1)$ przy mapowaniu $p=\frac{1-\langle Z\rangle}{2}$?
- 0.2
- 0.4
+ 0.8
- 0.6
! $(1+0.6)/2=0.8$.

? Która część VQC jest wykonywana na komputerze klasycznym?
- Kodowanie danych w stanie
- Splątywanie kubitów
+ Aktualizacja parametrów przez optymalizator
- Pomiar w bazie Z
! To hybryda: QPU liczy wartości oczekiwane, CPU optymalizuje.

? Ile obwodów (bez shotów) wymaga 1 epoka parameter‑shift dla 100 próbek i 20 parametrów (gradient + wartość funkcji)?
- 2000
+ 4100
- 120
- 40
! $100\cdot(1+2\cdot20)=4100$.

? Co jest dobrą praktyką przy raportowaniu wyników VQC?
+ Porównanie z klasyczną bazą o podobnej liczbie parametrów, na zbiorze testowym, dla wielu ziaren
- Raportowanie tylko najlepszego przebiegu na zbiorze treningowym
- Pomijanie modeli klasycznych
- Testowanie wyłącznie na bezszumowym symulatorze bez wzmianki o tym
! Rzetelna ewaluacja to podstawa w QML, gdzie łatwo o zawyżone deklaracje.
