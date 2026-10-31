---
id: qml-advantage
title: Przewaga kwantowa w ML i dekwantyzacja
summary: Kiedy QML może wygrać, a kiedy algorytmy klasyczne doganiają — Tang, Huang i „potęga danych”.
level: zaawansowany
---
### Rodzaje „przewagi”
- **obliczeniowa** (czas działania),
- **próbkowa** (liczba danych lub eksperymentów potrzebnych do nauczenia się),
- **zasobowa / energetyczna**.

Trzeba też odróżniać przewagę **dowiedlną**, **empiryczną** i **heurystyczną**.

### Dekwantyzacja
Ewin Tang (2018, wtedy jeszcze studentka): klasyczny algorytm rekomendacji działający w czasie **polilogarytmicznym** względem wymiaru — przy założeniu dostępu do danych przez struktury umożliwiające próbkowanie ($\ell^2$‑*sampling*), analogicznego do QRAM. To obaliło wykładniczą przewagę algorytmu Kerenidisa–Prakasha. Później zdekwantyzowano m.in. kwantowe PCA, regresję niskiego rzędu i niektóre metody oparte na HHL dla macierzy niskiego rzędu.

**Lekcja:** porównując z algorytmem używającym QRAM, trzeba dać algorytmowi klasycznemu równoważny dostęp do danych. Wtedy przewaga zostaje (zwykle wielomianowa) albo znika.

### Potęga danych (Huang i in., *Nature Communications* 2021)
„Power of data in quantum machine learning”: modele klasyczne **uczone na danych** (np. na wynikach obwodu kwantowego) potrafią skutecznie przewidywać zachowanie obwodów, których same nie umieją symulować. Autorzy zaproponowali **geometryczną różnicę** między kernelami klasycznym i kwantowym, by ocenić potencjalną przewagę przed treningiem, oraz **projected quantum kernels**.

### Gdzie przewaga jest udowodniona
- **dane kwantowe**: uczenie właściwości stanów i procesów — eksperymentalnie wykazano wykładniczą przewagę przy użyciu pamięci kwantowej (Huang i in., *Science* 2022, procesor Google),
- **problemy kryptograficzne**: Liu, Arunachalam, Temme (*Nature Physics* 2021) — kernel kwantowy dla problemu opartego na logarytmie dyskretnym,
- **modele generatywne**: istnieją rozkłady (np. z obwodów IQP), które QCBM reprezentuje, a modele klasyczne efektywnie nie (przy założeniach złożonościowych).

### Generalizacja
Caro i in. (*Nature Communications* 2022): błąd generalizacji modelu QML skaluje się jak $\tilde O\big(\sqrt{T/N}\big)$, gdzie $T$ to liczba trenowalnych bramek, a $N$ — liczba danych treningowych. Dobra wiadomość: małym modelom wystarcza mało danych. Ale dobra generalizacja nie oznacza niskiego błędu treningowego.

### Uczciwa ocena (benchmarki)
Bowles, Ahmed i Schuld (2024, „Better than classical? The subtle art of benchmarking quantum machine learning models”): na 12 popularnych modelach QML i wielu zbiorach danych **proste modele klasyczne zwykle wygrywały**, a usunięcie splątania często **nie pogarszało** wyników modeli kwantowych. Wniosek: QML potrzebuje rzetelnych benchmarków i lepszej teorii, a nie „magicznej przewagi”.

> [!key] Podsumowanie
> Najbardziej obiecujące: **dane kwantowe**, problemy ze strukturą trudną klasycznie, modele generatywne. Najmniej: klasyfikacja zwykłych danych tabelarycznych i obrazów na sprzęcie NISQ.

@@@ fiszki
Q: Czym jest dekwantyzacja?
A: Konstrukcją klasycznego algorytmu, który przy analogicznym dostępie do danych (np. $\ell^2$‑sampling) osiąga wydajność zbliżoną do algorytmu kwantowego, eliminując jego (wykładniczą) przewagę.

Q: Co zdekwantyzowała Ewin Tang w 2018 r.?
A: Kwantowy algorytm systemów rekomendacyjnych (Kerenidis–Prakash) — jej klasyczny algorytm działa w czasie polilogarytmicznym przy dostępie przez próbkowanie.

Q: Co pokazała praca „Power of data in QML” (Huang i in. 2021)?
A: Że modele klasyczne uczone na danych mogą przewidywać wyniki trudnych obwodów; zaproponowano miarę geometryczną potencjalnej przewagi i projected quantum kernels.

Q: Jak skaluje się błąd generalizacji modelu QML (Caro i in. 2022)?
A: Jak $\tilde O(\sqrt{T/N})$ — $T$ trenowalnych bramek, $N$ próbek treningowych.

Q: Gdzie przewaga kwantowa w uczeniu została wykazana eksperymentalnie?
A: W uczeniu z danych kwantowych z użyciem pamięci kwantowej (Huang i in., *Science* 2022).

Q: Jaki był główny wniosek benchmarku Bowles–Ahmed–Schuld (2024)?
A: Proste modele klasyczne zwykle wygrywały z popularnymi modelami QML, a usunięcie splątania często nie pogarszało wyników.

Q: Na czym opiera się dowiedlna przewaga kernela kwantowego Liu–Arunachalam–Temme?
A: Na problemie zbudowanym wokół logarytmu dyskretnego — trudnego klasycznie (przy założeniach kryptograficznych), a łatwego dla algorytmu Shora.

@@@ quiz
? Kto zdekwantyzował kwantowy algorytm rekomendacji?
+ Ewin Tang
- Peter Shor
- Lov Grover
- John Preskill
! W 2018 r., jeszcze jako studentka.

? Według Caro i in. błąd generalizacji modelu QML skaluje się jak:
- $\sqrt{N/T}$
+ $\sqrt{T/N}$
- $2^{-n}$
- $T\cdot N$
! $T$ — liczba trenowalnych bramek, $N$ — liczba danych.

? W którym obszarze przewaga kwantowa w uczeniu jest najlepiej uzasadniona?
- Klasyfikacja MNIST
+ Uczenie właściwości stanów i procesów kwantowych (dane kwantowe)
- Regresja cen mieszkań
- Analiza sentymentu
! Wyniki teoretyczne i eksperyment Google (*Science* 2022).

? Co należy zapewnić algorytmowi klasycznemu w uczciwym porównaniu z algorytmem używającym QRAM?
+ Równoważny dostęp do danych (np. struktury do próbkowania $\ell^2$)
- Mniejszy zbiór danych
- Więcej czasu na trening
- Dostęp do GPU
! Bez tego porównanie jest nieuczciwe — to główna lekcja dekwantyzacji.
%%%
---
id: qml-qdata
title: Uczenie z danych kwantowych i klasyczne cienie
summary: Tomografia, klasyczne cienie, rozpoznawanie faz i uczenie z pamięcią kwantową.
level: zaawansowany
---
### Dane kwantowe
To stany lub procesy pochodzące z eksperymentu albo obliczenia kwantowego: wyniki symulacji materiałów, stany czujników kwantowych, wyjścia innych obwodów. Tu QML ma najsilniejsze podstawy — nie trzeba „ładować” danych klasycznych, a dane mają naturalną strukturę kwantową.

### Problem: tomografia jest droga
Pełna tomografia stanu $n$ kubitów to ok. $4^n$ parametrów i wykładniczo wiele pomiarów. Zwykle potrzebujemy jednak tylko **wybranych własności** (wartości oczekiwanych, wierności, entropii).

### Klasyczne cienie (classical shadows)
Huang, Kueng i Preskill (*Nature Physics* 2020):
1. losowo obróć stan (np. losowa baza Pauliego na każdym kubicie lub losowa bramka Clifforda),
2. zmierz w bazie $Z$ i zapisz bazę oraz wynik,
3. z każdego „zdjęcia” zbuduj klasyczny estymator stanu, np. dla losowych Pauliego $\hat\rho=\bigotimes_j\big(3U_j^\dagger|b_j\rangle\langle b_j|U_j-I\big)$.

Z $N=O\big(\log(M)\,\max_i\|O_i\|^2_{\text{shadow}}/\varepsilon^2\big)$ pomiarów można przewidzieć jednocześnie **$M$ różnych** obserwabli. Dla obserwabli lokalnych (np. $k$‑lokalnych Pauliego) jest to bardzo tanie.

### Uczenie na cieniach
Klasyczne cienie stają się **cechami** dla klasycznych modeli ML. Huang i in. (*Science* 2022, „Provably efficient machine learning for quantum many‑body problems”): model klasyczny uczony na cieniach stanów podstawowych przewiduje własności nowych stanów z danej fazy i **klasyfikuje fazy materii** — z dowiedlną efektywnością.

### Uczenie z pamięcią kwantową
Gdy można przechowywać kopie stanów i wykonywać na nich wspólne (splątane) pomiary, niektóre zadania (np. przewidywanie wielu obserwabli Pauliego, uczenie procesów) wymagają **wykładniczo mniej** eksperymentów niż pomiary kopii pojedynczo. Pokazano to eksperymentalnie na procesorze Sycamore (Huang i in., *Science* 2022, „Quantum advantage in learning from experiments”).

### Zastosowania
- klasyfikacja faz kwantowych (QCNN, ML na cieniach),
- weryfikacja i kalibracja urządzeń kwantowych,
- dekodery kodów korekcji błędów oparte na sieciach neuronowych (np. AlphaQubit — Google DeepMind 2024),
- czujniki kwantowe połączone z ML.

@@@ fiszki
Q: Dlaczego pełna tomografia stanu jest niepraktyczna dla wielu kubitów?
A: Stan $n$ kubitów ma ok. $4^n$ parametrów, więc potrzeba wykładniczo wielu pomiarów.

Q: Na czym polegają klasyczne cienie?
A: Na losowych obrotach (np. Pauli/Clifford) i pomiarach w bazie Z, z których buduje się klasyczne estymatory stanu pozwalające przewidzieć wiele obserwabli naraz.

Q: Jak liczba pomiarów w klasycznych cieniach zależy od liczby przewidywanych obserwabli $M$?
A: Logarytmicznie: $O(\log M/\varepsilon^2)$ razy norma cienia obserwabli.

Q: Co wykazali Huang i in. w „Quantum advantage in learning from experiments” (2022)?
A: Że uczenie z pamięcią kwantową (wspólne pomiary wielu kopii) wymaga wykładniczo mniej eksperymentów dla niektórych zadań — pokazane na procesorze Google.

Q: Podaj przykład klasycznego ML na danych kwantowych.
A: Klasyfikacja faz materii na podstawie klasycznych cieni stanów albo sieć neuronowa dekodująca syndromy kodu powierzchniowego (np. AlphaQubit).

@@@ quiz
? Liczba pomiarów potrzebna w klasycznych cieniach do przewidzenia $M$ lokalnych obserwabli skaluje się jak:
- $M$
- $M^2$
+ $\log M$
- $4^n$
! To główna zaleta: logarytmiczna zależność od liczby obserwabli.

? Co oznacza „uczenie z pamięcią kwantową”?
+ Przechowywanie kopii stanów i wykonywanie na nich wspólnych, splątanych pomiarów
- Używanie klasycznej pamięci RAM w symulatorze
- Zapisywanie parametrów obwodu na dysku
- Używanie QRAM do ładowania danych klasycznych
! Daje to udowodnioną wykładniczą przewagę w niektórych zadaniach.

? Który przykład należy do kategorii QC (dane kwantowe, algorytm klasyczny)?
+ Sieć neuronowa dekodująca syndromy kodu powierzchniowego
- VQC na zbiorze Iris
- Algorytm Grovera
- QAOA dla MaxCut
! Dane (syndromy) pochodzą z układu kwantowego, a model jest klasyczny.
%%%
---
id: qml-tools
title: Narzędzia: PennyLane, Qiskit ML i inne
summary: Jak wygląda kod QML w praktyce — QNode, urządzenia, szablony, kernele i integracja z PyTorch.
level: średni
---
### PennyLane (Xanadu)
Biblioteka do **różniczkowalnego programowania kwantowego**. Kluczowe pojęcia:
- `qml.device("default.qubit", wires=n)` — symulator (także `lightning.qubit`; sprzęt przez wtyczki: IBM, IonQ, Amazon Braket…),
- `@qml.qnode(dev)` — funkcja kwantowa (obwód), którą różniczkuje się jak zwykłą funkcję,
- `diff_method`: `"backprop"`, `"adjoint"`, `"parameter-shift"`, `"finite-diff"`, `"spsa"`,
- szablony: `AngleEmbedding`, `AmplitudeEmbedding`, `IQPEmbedding`, `StronglyEntanglingLayers`, `BasicEntanglerLayers`,
- integracja z NumPy (autograd), PyTorch, JAX i TensorFlow.

```python
import pennylane as qml
from pennylane import numpy as np

dev = qml.device("default.qubit", wires=2)

@qml.qnode(dev, diff_method="parameter-shift")
def f(theta):
    qml.RY(theta[0], wires=0)
    qml.RX(theta[1], wires=1)
    qml.CNOT(wires=[0, 1])
    return qml.expval(qml.PauliZ(1))

theta = np.array([0.3, 0.8], requires_grad=True)
print(f(theta), qml.grad(f)(theta))
print(qml.draw(f)(theta))
```

Kernel kwantowy w PennyLane (compute–uncompute):
```python
@qml.qnode(dev)
def kernel_circuit(x1, x2):
    qml.AngleEmbedding(x1, wires=range(2))
    qml.adjoint(qml.AngleEmbedding)(x2, wires=range(2))
    return qml.probs(wires=range(2))

def k(x1, x2):
    return kernel_circuit(x1, x2)[0]   # p(00) = |<φ(x2)|φ(x1)>|^2
```

### Qiskit + Qiskit Machine Learning (IBM)
- obwody: `QuantumCircuit`, `ParameterVector`; biblioteka obwodów: `ZZFeatureMap`, `RealAmplitudes`, `EfficientSU2`,
- prymitywy: **Sampler** (rozkłady bitstringów) i **Estimator** (wartości oczekiwane),
- `qiskit_machine_learning`: `FidelityQuantumKernel`, `QSVC`, `VQC`, `EstimatorQNN`, `SamplerQNN`, `TorchConnector`, `NeuralNetworkClassifier`.

```python
from qiskit.circuit.library import ZZFeatureMap, RealAmplitudes
from qiskit_machine_learning.algorithms import VQC
from qiskit_machine_learning.optimizers import COBYLA

vqc = VQC(
    feature_map=ZZFeatureMap(2),
    ansatz=RealAmplitudes(2, reps=2),
    optimizer=COBYLA(maxiter=100),
)
vqc.fit(X_train, y_train)
print(vqc.score(X_test, y_test))
```

> [!warn] Wersje API
> Biblioteki kwantowe szybko się zmieniają (np. przejście Qiskit 1.x → 2.x, przeniesienie optymalizatorów do `qiskit_machine_learning`). Zawsze sprawdzaj dokumentację dla swojej wersji.

### Inne narzędzia
- **TensorFlow Quantum** + **Cirq** (Google) — obwody jako tensory w TF,
- **Amazon Braket**, **Azure Quantum** — dostęp do sprzętu wielu producentów,
- **CUDA‑Q** (NVIDIA) — szybkie symulacje na GPU,
- **TorchQuantum**, **Qibo** — symulatory zorientowane na ML,
- symulatory **sieci tensorowych** (np. `quimb`) — dla płytkich obwodów z wieloma kubitami.

@@@ fiszki
Q: Czym jest QNode w PennyLane?
A: Funkcją kwantową (obwód + urządzenie) opakowaną dekoratorem `@qml.qnode(dev)`, którą można różniczkować jak zwykłą funkcję.

Q: Jakie metody różniczkowania oferuje PennyLane?
A: Np. `backprop`, `adjoint`, `parameter-shift`, `finite-diff`, `spsa` (parametr `diff_method`).

Q: Czym różnią się prymitywy Sampler i Estimator w Qiskit?
A: Sampler zwraca rozkłady (quasi‑prawdopodobieństwa) bitstringów, a Estimator — wartości oczekiwane obserwabli.

Q: Która klasa Qiskit ML oblicza kernel jako wierność stanów?
A: `FidelityQuantumKernel` (używana np. z `QSVC` albo `SVC(kernel="precomputed")`).

Q: Jak w PennyLane policzyć kernel metodą compute–uncompute?
A: Zakodować $x_1$, zastosować `qml.adjoint` kodowania dla $x_2$ i zwrócić `qml.probs`; kernel to $p(0\dots0)$.

Q: Jak połączyć model Qiskit z PyTorch?
A: Przez `TorchConnector` opakowujący sieć QNN (EstimatorQNN lub SamplerQNN).

@@@ quiz
? Które wywołanie tworzy symulator wektora stanu w PennyLane?
+ `qml.device("default.qubit", wires=2)`
- `QuantumCircuit(2)`
- `cirq.Simulator()`
- `SVC(kernel="precomputed")`
! Pozostałe dotyczą Qiskit, Cirq i scikit-learn.

? Która klasa Qiskit ML jest wariacyjnym klasyfikatorem?
- `FidelityQuantumKernel`
+ `VQC`
- `ZZFeatureMap`
- `Sampler`
! VQC łączy mapę cech, ansatz i optymalizator.

? Do czego służy `qml.adjoint` w obwodzie kernela?
+ Do odwrócenia (sprzężenia hermitowskiego) obwodu kodującego dla drugiego punktu
- Do obliczenia gradientu metodą adjoint
- Do pomiaru w bazie X
- Do dodania szumu
! Obwód $U^\dagger(\mathbf x_2)U(\mathbf x_1)|0\rangle$ — prawdopodobieństwo wyniku $|0\dots0\rangle$ to kernel.
%%%
---
id: qml-review
title: Powtórka: QML w 20 faktach
summary: Skondensowana ściąga przed egzaminem lub rozmową — najważniejsze wzory, nazwiska i pułapki.
level: średni
---
1. Model wariacyjny: $f(\mathbf x)=\langle0|U^\dagger(\mathbf x,\boldsymbol\theta)MU(\mathbf x,\boldsymbol\theta)|0\rangle$; trening hybrydowy (QPU + CPU).
2. Taksonomia **CC/CQ/QC/QQ**: pierwsza litera — dane, druga — algorytm.
3. **Kodowania**: bazowe ($kd$ kubitów), amplitudowe ($\log_2d$ kubitów, głębokość $O(d)$), kątowe ($d$ kubitów, głębokość 1), IQP/ZZ (splątanie, iloczyny cech).
4. Modele kwantowe są **liniowe w przestrzeni cech** $\rho(\mathbf x)$ → to metody jądrowe (Schuld 2021).
5. **Kernel kwantowy**: $k(\mathbf x,\mathbf x')=|\langle\phi(\mathbf x)|\phi(\mathbf x')\rangle|^2$; estymacja: compute–uncompute ($n$ kubitów) lub swap test ($2n+1$).
6. **QSVM**: kernel na QPU, SVM klasycznie — **problem wypukły**; koszt $O(m^2)$ obwodów.
7. **Koncentracja kerneli**: dla wielu kubitów wartości są prawie stałe → wykładniczo wiele shotów; ratunek: mała szerokość pasma, projected kernels.
8. **Parameter‑shift**: $\partial f=\frac12[f(\theta+\frac{\pi}{2})-f(\theta-\frac{\pi}{2})]$ — dokładny, $2P$ obwodów na gradient.
9. **SPSA**: 2 obwody na krok; **adjoint** i backprop — tylko w symulatorach.
10. Szum shotów $\sim1/\sqrt S$; różnice skończone wzmacniają go jak $1/h$.
11. **Data re‑uploading** (Pérez‑Salinas 2020): 1 kubit to uniwersalny klasyfikator.
12. Model to **szereg Fouriera** (Schuld–Sweke–Meyer 2021): częstotliwości z kodowania, współczynniki z parametrów.
13. **Barren plateaus** (McClean 2018): $\mathrm{Var}[\partial\mathcal L]\sim b^{-n}$; przyczyny: ekspresywność, globalny koszt, splątanie, szum.
14. Obrona przed BP: lokalne koszty, płytkie i symetryczne ansatze, inicjalizacja identycznością, trening warstwowy.
15. Brak BP często oznacza **symulowalność klasyczną** (Cerezo i in. 2023).
16. **QCNN** (Cong–Choi–Lukin 2019): $O(\log n)$ parametrów, bez BP, fazy kwantowe.
17. **Dekwantyzacja** (Tang 2018): przy dostępie przez próbkowanie algorytmy klasyczne doganiają „wykładnicze” QML oparte na QRAM.
18. **Generalizacja**: $\tilde O(\sqrt{T/N})$ (Caro i in. 2022).
19. Przewaga udowodniona: dane kwantowe (pamięć kwantowa, *Science* 2022) i problemy kryptograficzne (Liu i in. 2021).
20. Rzetelny benchmark: porównuj z klasyczną bazą; proste modele klasyczne często wygrywają (Bowles i in. 2024).

> [!tip] Następny krok
> Zrób quiz z działu QML w trybie „Moje błędy”, a potem przejdź fiszki działu.

@@@ quiz
? Który zestaw poprawnie opisuje koszt metod estymacji kernela?
+ compute–uncompute: $n$ kubitów i podwójna głębokość; swap test: $2n+1$ kubitów
- compute–uncompute: $2n$ kubitów; swap test: $n$ kubitów
- obie metody wymagają QRAM
- obie metody wymagają tylko jednego kubitu
! Swap test potrzebuje dwóch rejestrów i kubitu pomocniczego.

? Co wyznacza częstotliwości szeregu Fouriera modelu kwantowego?
- Liczba shotów
- Wybór optymalizatora
+ Generatory i liczba powtórzeń kodowania danych
- Liczba epok
! Parametry wpływają tylko na współczynniki.

? Która metoda liczenia gradientu jest dokładna i działa na sprzęcie?
+ parameter‑shift
- backprop
- adjoint
- różnice skończone
! Backprop i adjoint wymagają symulatora, a różnice skończone są obciążone.

? Który model ma udowodniony brak barren plateaus i $O(\log n)$ parametrów?
- Hardware‑efficient ansatz z 50 warstwami
+ QCNN
- Losowy obwód (2‑design)
- QAOA z dużym $p$
! Pesah i in. 2021.
