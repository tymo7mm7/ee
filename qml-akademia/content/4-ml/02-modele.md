---
id: ml-trees
title: Drzewa decyzyjne i zespoły modeli
summary: Gini i entropia, Random Forest, boosting i dlaczego GBDT królują na danych tabelarycznych.
level: średni
---
### Drzewo decyzyjne
Rekurencyjnie dzieli przestrzeń cech pytaniami typu $x_j\le t$. Podział wybiera się tak, by najbardziej zmniejszyć nieczystość węzła:
- **indeks Giniego**: $G=1-\sum_kp_k^2$,
- **entropia**: $H=-\sum_kp_k\log_2p_k$ (spadek entropii = *information gain*),
- w regresji: spadek wariancji (MSE).

Zalety: interpretowalność, brak potrzeby skalowania cech, obsługa cech kategorycznych. Wada: **wysoka wariancja** — głębokie drzewo łatwo się przeucza (przycinanie, `max_depth`, `min_samples_leaf`).

### Bagging i Random Forest
**Bagging**: wiele modeli trenowanych na próbach bootstrapowych, predykcje uśredniane → mniejsza wariancja. **Random Forest** (Breiman 2001) dodatkowo losuje podzbiór cech przy każdym podziale (np. $\sqrt d$), co dekoreluje drzewa. Bonus: błąd **out‑of‑bag** jako darmowa walidacja i miary ważności cech.

### Boosting
Modele uczone **sekwencyjnie** — każdy poprawia błędy poprzednich, co redukuje obciążenie:
- **AdaBoost** — zwiększa wagi źle sklasyfikowanych przykładów,
- **Gradient Boosting** — każde kolejne drzewo dopasowuje się do **ujemnego gradientu straty** (dla MSE: do reszt); krok jest skurczony przez *learning rate*,
- implementacje: **XGBoost**, **LightGBM**, **CatBoost** — de facto standard dla danych tabelarycznych.

### Bagging vs boosting
| | Bagging (RF) | Boosting (GBDT) |
|---|---|---|
| trening | równoległy | sekwencyjny |
| redukuje głównie | wariancję | obciążenie |
| modele bazowe | głębokie drzewa | płytkie drzewa |
| wrażliwość na hiperparametry | mała | większa |

> [!info] Kontekst QML
> W benchmarkach QML na danych tabelarycznych dobrze ustawione GBDT lub Random Forest to bardzo mocna klasyczna baza — warto zawsze je uwzględniać.

@@@ fiszki
Q: Zapisz indeks Giniego.
A: $G=1-\sum_kp_k^2$, gdzie $p_k$ to udział klasy $k$ w węźle.

Q: Czym Random Forest różni się od zwykłego baggingu drzew?
A: Dodatkowo losuje podzbiór cech przy każdym podziale, co dekoreluje drzewa.

Q: Do czego dopasowuje się kolejne drzewo w gradient boostingu?
A: Do ujemnego gradientu funkcji straty względem bieżących predykcji (dla MSE — do reszt).

Q: Co redukuje bagging, a co boosting?
A: Bagging — głównie wariancję; boosting — głównie obciążenie.

Q: Czym jest błąd out‑of‑bag?
A: Błędem liczonym na przykładach, które nie trafiły do próby bootstrapowej danego drzewa — darmową estymatą błędu testowego.

@@@ quiz
? Węzeł zawiera 50% klasy A i 50% klasy B. Ile wynosi indeks Giniego?
- 0
+ 0.5
- 1
- 0.25
! $1-(0.5^2+0.5^2)=0.5$.

? Który algorytm buduje drzewa sekwencyjnie, korygując błędy poprzednich?
- Random Forest
+ Gradient Boosting
- k‑NN
- Bagging
! Boosting jest sekwencyjny.

? Jaka jest główna wada pojedynczego głębokiego drzewa decyzyjnego?
+ Wysoka wariancja (skłonność do przeuczenia)
- Brak interpretowalności
- Konieczność skalowania cech
- Brak obsługi klasyfikacji
! Małe zmiany w danych mogą całkowicie zmienić drzewo.
%%%
---
id: ml-knn
title: k‑NN i naiwny klasyfikator Bayesa
summary: Dwa proste, ale ważne punkty odniesienia: uczenie leniwe i model probabilistyczny.
level: podstawowy
---
### k najbliższych sąsiadów (k‑NN)
Brak treningu („uczenie leniwe”): dla nowego $\mathbf x$ znajdujemy $k$ najbliższych punktów treningowych i:
- w klasyfikacji — głosujemy większościowo (ewentualnie z wagami zależnymi od odległości),
- w regresji — uśredniamy ich wartości.

Wybór $k$: małe $k$ → złożona, zaszumiona granica (wariancja); duże $k$ → gładka granica (obciążenie). Konieczne jest **skalowanie cech** (odległość euklidesowa!). Koszt predykcji $O(md)$ (przyspieszenia: KD‑drzewa, przybliżone wyszukiwanie — FAISS, HNSW).

**Przekleństwo wymiarowości**: w wysokim wymiarze wszystkie punkty są niemal jednakowo odległe — pojęcie sąsiedztwa traci sens. (Analogia do koncentracji kerneli kwantowych!)

### Naiwny klasyfikator Bayesa
Z twierdzenia Bayesa i założenia **warunkowej niezależności cech**:
$$p(y\mid\mathbf x)\propto p(y)\prod_{j=1}^dp(x_j\mid y).$$
Warianty: gaussowski (cechy ciągłe), multinomialny (zliczenia słów — klasyfikacja tekstu, filtry spamu), Bernoulliego. **Wygładzanie Laplace'a** zapobiega zerowym prawdopodobieństwom. Model jest szybki i działa dobrze przy małych danych, choć założenie niezależności rzadko jest spełnione.

### Model generatywny vs dyskryminatywny
- **generatywny** — modeluje $p(\mathbf x,y)=p(\mathbf x|y)p(y)$ (naiwny Bayes, GMM),
- **dyskryminatywny** — modeluje bezpośrednio $p(y|\mathbf x)$ lub granicę decyzyjną (regresja logistyczna, SVM).

@@@ fiszki
Q: Jak działa k‑NN?
A: Dla nowego punktu znajduje $k$ najbliższych przykładów treningowych i głosuje (klasyfikacja) lub uśrednia (regresja).

Q: Jak wybór $k$ wpływa na k‑NN?
A: Małe $k$ — złożona granica i wysoka wariancja; duże $k$ — gładka granica i wysokie obciążenie.

Q: Czym jest przekleństwo wymiarowości?
A: W wysokim wymiarze dane są rzadkie, a odległości między punktami stają się podobne — metody oparte na odległości tracą moc.

Q: Jakie założenie czyni naiwny Bayes?
A: Że cechy są warunkowo niezależne przy danej klasie: $p(\mathbf x|y)=\prod_jp(x_j|y)$.

Q: Po co wygładzanie Laplace'a w naiwnym Bayesie?
A: By uniknąć zerowych prawdopodobieństw dla cech niewidzianych w danej klasie (dodaje pseudo‑zliczenia).

@@@ quiz
? Dlaczego przed k‑NN skaluje się cechy?
+ Bo cechy o dużym zakresie zdominowałyby odległość
- Bo k‑NN wymaga cech binarnych
- Aby zmniejszyć $k$
- Bo inaczej nie da się obliczyć gradientu
! Odległość euklidesowa jest wrażliwa na skalę.

? Naiwny Bayes jest przykładem modelu:
+ generatywnego
- dyskryminatywnego
- nienadzorowanego
- ze wzmocnieniem
! Modeluje $p(\mathbf x|y)p(y)$.

? Ile kosztuje trening k‑NN?
+ Praktycznie nic — model tylko zapamiętuje dane
- $O(m^3)$
- $O(d^3)$
- Zależy od liczby epok
! Cały koszt przenosi się na predykcję.
%%%
---
id: ml-unsup
title: Uczenie nienadzorowane: klasteryzacja i PCA
summary: k‑means, DBSCAN, klasteryzacja hierarchiczna, PCA, t‑SNE/UMAP i autoenkodery.
level: średni
---
### k‑means
Minimalizuje sumę kwadratów odległości punktów od centroidów:
$$\min_{C_1,\dots,C_k}\sum_{j=1}^{k}\sum_{\mathbf x\in C_j}\|\mathbf x-\boldsymbol\mu_j\|^2.$$
Algorytm Lloyda: przypisz punkty do najbliższego centroidu → przelicz centroidy → powtarzaj. Zbiega do minimum lokalnego (pomaga inicjalizacja **k‑means++**). Zakłada „kuliste” klastry podobnej wielkości. Wybór $k$: metoda łokcia, współczynnik **silhouette**.

### Inne metody klasteryzacji
- **DBSCAN** — gęstościowa: klastry dowolnego kształtu, wykrywa szum; parametry $\varepsilon$ i `min_samples`,
- **hierarchiczna aglomeracyjna** — łączy najbliższe klastry (linkage: single, complete, average, Ward); wynik to dendrogram,
- **GMM** — mieszanina rozkładów gaussowskich, algorytm EM, „miękkie” przypisanie do klastrów.

### PCA — analiza składowych głównych
Szukamy kierunków maksymalnej wariancji. Dla wycentrowanych danych $X$:
$$\Sigma=\tfrac1mX^\top X,\qquad \Sigma\mathbf v_k=\lambda_k\mathbf v_k.$$
Rzut na $k$ wektorów własnych o największych $\lambda$ zachowuje ułamek $\frac{\sum_{i\le k}\lambda_i}{\sum_i\lambda_i}$ wariancji. W praktyce liczy się to przez SVD: $X=USV^\top$. PCA jest liniowa; cechy warto najpierw standaryzować.

> [!info] PCA w QML
> Obwody NISQ mają mało kubitów, więc dane (np. obrazy 28×28) zwykle redukuje się PCA do 4–16 cech przed kodowaniem. Uwaga: wtedy dużą część „pracy” wykonuje klasyczny preprocessing.

### Wizualizacja nieliniowa
**t‑SNE** i **UMAP** zachowują lokalne sąsiedztwo — świetne do wizualizacji w 2D. Odległości między klastrami na takim wykresie nie są wiarygodne.

### Autoenkodery
Sieć enkoder → wąskie gardło → dekoder uczy się odtwarzać wejście; kod w wąskim gardle to nieliniowa reprezentacja niskowymiarowa. Warianty: VAE (generatywny), autoenkoder odszumiający.

@@@ fiszki
Q: Jaką funkcję minimalizuje k‑means?
A: Sumę kwadratów odległości punktów od centroidów ich klastrów (inercję).

Q: Jakie są ograniczenia k‑means?
A: Trzeba podać $k$, algorytm zakłada kuliste klastry podobnej wielkości, zbiega do minimum lokalnego i jest wrażliwy na skalę oraz wartości odstające.

Q: Czym DBSCAN różni się od k‑means?
A: Jest gęstościowy — znajduje klastry dowolnego kształtu, oznacza szum i nie wymaga podania $k$.

Q: Jak PCA wyznacza składowe główne?
A: Jako wektory własne macierzy kowariancji (lub prawe wektory osobliwe z SVD) uporządkowane według wartości własnych (wariancji).

Q: Jaką część wariancji zachowuje rzut na $k$ składowych głównych?
A: $\sum_{i\le k}\lambda_i/\sum_i\lambda_i$.

Q: Do czego służą t‑SNE i UMAP?
A: Do nieliniowej wizualizacji danych w 2D/3D z zachowaniem lokalnego sąsiedztwa.

@@@ quiz
? Która metoda wykrywa klastry o dowolnym kształcie i oznacza punkty szumu?
- k‑means
+ DBSCAN
- PCA
- Regresja liniowa
! Opiera się na gęstości punktów.

? Wartości własne macierzy kowariancji to 6, 3 i 1. Ile wariancji zachowują dwie pierwsze składowe?
- 60%
+ 90%
- 30%
- 100%
! $(6+3)/10=0.9$.

? Dlaczego przed PCA zwykle standaryzuje się cechy?
+ Bo PCA maksymalizuje wariancję, więc cechy o dużej skali zdominowałyby składowe
- Bo PCA wymaga danych binarnych
- Bo inaczej macierz kowariancji nie istnieje
- Aby zwiększyć liczbę składowych
! Np. ta sama wielkość w metrach i w milimetrach dałaby zupełnie inne składowe.
%%%
---
id: ml-nn
title: Sieci neuronowe i propagacja wsteczna
summary: Perceptron, MLP, funkcje aktywacji, backprop, inicjalizacja i znikające gradienty.
level: średni
lab: train
---
### Od perceptronu do MLP
Neuron: $a=\sigma(\mathbf w^\top\mathbf x+b)$. **Perceptron** (Rosenblatt 1958) z funkcją progową rozdziela tylko dane liniowo separowalne (Minsky i Papert: nie rozwiąże XOR). **MLP** (perceptron wielowarstwowy) składa warstwy:
$$\mathbf h^{(l)}=\sigma\big(W^{(l)}\mathbf h^{(l-1)}+\mathbf b^{(l)}\big).$$
**Twierdzenie o uniwersalnej aproksymacji**: jedna warstwa ukryta z dostatecznie wieloma neuronami i nieliniową aktywacją aproksymuje dowolną funkcję ciągłą na zbiorze zwartym.

### Funkcje aktywacji
| Funkcja | Wzór | Uwagi |
|---|---|---|
| sigmoida | $1/(1+e^{-z})$ | nasyca się → znikające gradienty |
| tanh | $\tanh z$ | wycentrowana w 0, też się nasyca |
| **ReLU** | $\max(0,z)$ | standard; problem „martwych neuronów” |
| Leaky ReLU, GELU, SiLU | — | GELU w transformerach |
| softmax | $e^{z_k}/\sum_j e^{z_j}$ | wyjście wieloklasowe |

### Propagacja wsteczna (backprop)
Reguła łańcuchowa stosowana od wyjścia do wejścia. Dla warstwy $l$:
$$\boldsymbol\delta^{(l)}=\big(W^{(l+1)\top}\boldsymbol\delta^{(l+1)}\big)\odot\sigma'(\mathbf z^{(l)}),\qquad \frac{\partial\mathcal L}{\partial W^{(l)}}=\boldsymbol\delta^{(l)}\mathbf h^{(l-1)\top}.$$
Gradient względem **wszystkich** parametrów kosztuje mniej więcej tyle co 1–2 przebiegi w przód (automatyczne różniczkowanie w trybie odwrotnym). To zasadnicza przewaga nad regułą parameter‑shift w QML ($2P$ obwodów).

### Problemy treningu
- **znikające / eksplodujące gradienty** w głębokich sieciach → ReLU, **inicjalizacja** Xaviera/Glorota (tanh) i He (ReLU), **połączenia rezydualne** (ResNet), normalizacja (BatchNorm, LayerNorm), gradient clipping,
- przeuczenie → dropout, weight decay, augmentacja, early stopping.

> [!info] Analogia
> Znikające gradienty w głębokich sieciach ↔ **barren plateaus** w PQC. Różnica: w PQC zanik jest wykładniczy w liczbie kubitów i nie ma prostego odpowiednika połączeń rezydualnych.

@@@ fiszki
Q: Co mówi twierdzenie o uniwersalnej aproksymacji?
A: Sieć z jedną warstwą ukrytą i nieliniową aktywacją może dowolnie dokładnie aproksymować każdą funkcję ciągłą na zbiorze zwartym (przy dostatecznej liczbie neuronów).

Q: Dlaczego perceptron nie rozwiąże XOR?
A: Bo jego granica decyzyjna jest liniowa, a XOR nie jest liniowo separowalny.

Q: Ile kosztuje backpropagation w zależności od liczby parametrów?
A: Gradient wszystkich parametrów kosztuje rząd jednego–dwóch przebiegów w przód (niezależnie od liczby parametrów).

Q: Jakie są sposoby walki ze znikającymi gradientami?
A: ReLU, inicjalizacja Xaviera/He, połączenia rezydualne, normalizacja warstw, odpowiednia architektura.

Q: Kiedy stosować inicjalizację He, a kiedy Xaviera?
A: He — dla ReLU (wariancja $2/n_{in}$), Xaviera/Glorota — dla tanh i sigmoidy.

Q: Czym jest problem „martwych neuronów” ReLU?
A: Neuron z ujemnym wejściem dla wszystkich danych ma zerowy gradient i przestaje się uczyć; pomagają Leaky ReLU lub GELU.

@@@ quiz
? Która funkcja aktywacji jest standardem w ukrytych warstwach głębokich sieci?
- sigmoida
+ ReLU (i jej warianty)
- funkcja skokowa
- funkcja liniowa
! Nie nasyca się dla $z>0$.

? Dlaczego sieć złożona wyłącznie z warstw liniowych (bez aktywacji) ma ograniczoną moc?
+ Bo złożenie przekształceń liniowych jest nadal przekształceniem liniowym
- Bo nie da się policzyć gradientu
- Bo ma za mało parametrów
- Bo nie działa na GPU
! $W_2W_1\mathbf x=W\mathbf x$.

? Ile obwodów wymaga parameter‑shift dla 1000 parametrów (na jedną próbkę), a ile przebiegów sieci wymaga backprop?
+ 2000 obwodów vs ok. 1 przebieg w przód i 1 w tył
- 1000 vs 1000
- 2 vs 2000
- 1 vs 1
! To fundamentalna różnica w skalowaniu między QML a klasycznym deep learningiem.
%%%
---
id: ml-deep
title: Głębokie architektury: CNN, RNN, Transformer
summary: Konwolucje, rekurencja, mechanizm uwagi i uczenie transferowe.
level: średni
---
### Sieci konwolucyjne (CNN)
Warstwa konwolucyjna przesuwa mały filtr (np. 3×3) po obrazie: **lokalność** i **współdzielenie wag** dają mało parametrów i równoważność względem przesunięć. **Pooling** (max/avg) zmniejsza rozdzielczość. Architektury: LeNet, AlexNet (2012), VGG, **ResNet** (połączenia rezydualne, 2015), EfficientNet. Kwantowy odpowiednik: QCNN.

### Sieci rekurencyjne (RNN)
Stan ukryty $\mathbf h_t=f(W\mathbf h_{t-1}+U\mathbf x_t)$ przetwarza sekwencje. Problem: znikające gradienty w czasie → **LSTM** (bramki: wejściowa, zapominająca, wyjściowa; komórka pamięci) i **GRU**. W NLP wyparte dziś przez transformery.

### Transformer i uwaga
„Attention is all you need” (Vaswani i in. 2017). **Uwaga skalowana iloczynem skalarnym**:
$$\mathrm{Attention}(Q,K,V)=\mathrm{softmax}\Big(\frac{QK^\top}{\sqrt{d_k}}\Big)V$$
- $Q$, $K$, $V$ — zapytania, klucze i wartości (liniowe rzuty wejścia),
- **multi‑head** — kilka równoległych głów uwagi,
- **kodowanie pozycji** (sinusoidalne, RoPE) — bo sama uwaga nie zna kolejności tokenów,
- bloki: uwaga + MLP, połączenia rezydualne, LayerNorm.

Koszt uwagi: $O(n^2d)$ dla sekwencji długości $n$. Warianty: enkoder (BERT), dekoder (GPT — modele językowe), enkoder‑dekoder (T5), Vision Transformer (obraz jako sekwencja łatek).

### Embeddingi i transfer learning
**Embedding** to gęsta reprezentacja wektorowa (słowa, tokenu, obrazu). **Transfer learning**: model pretrenowany na dużych danych jest dostrajany (*fine‑tuning*) do nowego zadania; LoRA dostraja tylko małe macierze niskiego rzędu.

### Modele generatywne (w skrócie)
VAE, GAN (generator kontra dyskryminator), **modele dyfuzyjne** (odszumianie krok po kroku), autoregresyjne modele językowe.

@@@ fiszki
Q: Jakie dwie własności konwolucji zmniejszają liczbę parametrów CNN?
A: Lokalność połączeń (mały filtr) i współdzielenie wag (ten sam filtr w każdym miejscu).

Q: Zapisz wzór uwagi skalowanej iloczynem skalarnym.
A: $\mathrm{softmax}(QK^\top/\sqrt{d_k})V$.

Q: Po co w transformerze kodowanie pozycji?
A: Bo mechanizm uwagi nie zależy od kolejności wejść (jest niezmienniczy względem permutacji) — bez pozycji model nie zna kolejności tokenów.

Q: Jaki problem RNN rozwiązuje LSTM?
A: Znikające gradienty w długich sekwencjach — dzięki komórce pamięci i bramkom (wejściowej, zapominającej, wyjściowej).

Q: Jaka jest złożoność uwagi względem długości sekwencji $n$?
A: Kwadratowa: $O(n^2d)$.

Q: Co wprowadził ResNet?
A: Połączenia rezydualne $\mathbf h+F(\mathbf h)$, które umożliwiają trenowanie bardzo głębokich sieci.

@@@ quiz
? Po co dzieli się $QK^\top$ przez $\sqrt{d_k}$?
+ Aby iloczyny skalarne nie były zbyt duże i softmax się nie nasycał (stabilne gradienty)
- Aby zmniejszyć liczbę parametrów
- Aby dodać informację o pozycji
- Aby uwaga była symetryczna
! Wariancja iloczynu skalarnego rośnie z $d_k$.

? Który model to dekoder transformera trenowany autoregresyjnie?
- BERT
+ GPT
- ResNet
- LSTM
! GPT przewiduje kolejny token.

? Co oznacza współdzielenie wag w CNN?
+ Ten sam filtr jest stosowany w każdym miejscu obrazu
- Wszystkie warstwy mają te same wagi
- Wagi są kopiowane między sieciami
- Wagi są losowane w każdej epoce
! Daje to równoważność względem przesunięć.
%%%
---
id: ml-practice
title: Praktyka ML: dane, cechy, strojenie, pipeline
summary: Preprocessing, inżynieria cech, strojenie hiperparametrów i powtarzalny pipeline w scikit‑learn.
level: podstawowy
---
### Przygotowanie danych
- **braki danych**: usunięcie, imputacja (średnia, mediana, model), flaga „brak”,
- **cechy kategoryczne**: one‑hot encoding, kodowanie porządkowe, target encoding (uwaga na wyciek!),
- **skalowanie**: standaryzacja $(x-\mu)/\sigma$ lub min‑max do $[0,1]$ — ważne dla k‑NN, SVM, sieci, PCA i **kodowania kątowego w QML**,
- **wartości odstające**: transformacje (np. logarytm), metody odporne,
- **niezbalansowane klasy**: wagi klas, oversampling (SMOTE), undersampling.

### Inżynieria i selekcja cech
Cechy domenowe, interakcje, transformacje; selekcja: filtry (korelacja, informacja wzajemna), wrappery (RFE), metody wbudowane (L1, ważność cech w drzewach).

### Strojenie hiperparametrów
- **grid search** — pełna siatka (kosztowne),
- **random search** — losowe próbki (często lepsze przy wielu hiperparametrach),
- **optymalizacja bayesowska** (Optuna, Hyperopt) — model zastępczy wybiera obiecujące punkty,
- zawsze na zbiorze **walidacyjnym** albo w walidacji krzyżowej.

### Pipeline (scikit‑learn)
Łączy preprocessing z modelem, dzięki czemu walidacja krzyżowa nie powoduje wycieku danych:
```python
from sklearn.pipeline import make_pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVC
from sklearn.model_selection import GridSearchCV

pipe = make_pipeline(StandardScaler(), SVC(kernel="rbf"))
grid = GridSearchCV(pipe, {"svc__C": [0.1, 1, 10],
                           "svc__gamma": ["scale", 0.1, 1]}, cv=5)
grid.fit(X_train, y_train)
print(grid.best_params_, grid.score(X_test, y_test))
```

### Powtarzalność i ewaluacja
Ustalaj ziarna losowe, wersjonuj dane i kod, raportuj średnią ± odchylenie z kilku przebiegów, używaj właściwej metryki i zawsze porównuj z prostą bazą (np. `DummyClassifier`, regresja logistyczna).

@@@ fiszki
Q: Dlaczego skalowanie cech jest ważne dla SVM, k‑NN i sieci neuronowych?
A: Bo opierają się na odległościach, iloczynach skalarnych lub gradientach — cechy o dużej skali dominują i spowalniają trening.

Q: Czym jest one‑hot encoding?
A: Zamianą cechy kategorycznej o $K$ wartościach na $K$ kolumn binarnych (dokładnie jedna jedynka).

Q: Dlaczego random search bywa lepszy od grid search?
A: Przy wielu hiperparametrach ważnych jest zwykle tylko kilka; losowanie bada więcej różnych wartości tych ważnych przy tym samym budżecie.

Q: Po co używać Pipeline w scikit‑learn?
A: Aby preprocessing był dopasowywany tylko na foldach treningowych (brak wycieku) i by cały proces był powtarzalny.

Q: Jak radzić sobie z niezbalansowanymi klasami?
A: Wagi klas, oversampling (SMOTE), undersampling, odpowiednie metryki (F1, PR‑AUC), dobór progu decyzji.

@@@ quiz
? Gdzie dopasować StandardScaler w poprawnej procedurze?
+ Tylko na zbiorze treningowym (np. wewnątrz Pipeline)
- Na całym zbiorze przed podziałem
- Na zbiorze testowym
- Osobno dla każdej próbki
! Inaczej statystyki zbioru testowego przeciekają do treningu.

? Która metoda strojenia buduje model zastępczy funkcji celu?
- Grid search
- Random search
+ Optymalizacja bayesowska
- Walidacja krzyżowa
! Np. procesy gaussowskie lub TPE (Optuna).

? Po co porównywać model z `DummyClassifier`?
+ Aby sprawdzić, czy model jest lepszy od trywialnej bazy (np. zawsze klasa większościowa)
- Bo zawsze jest najlepszy
- Aby przyspieszyć trening
- Bo wymaga tego scikit-learn
! Szczególnie ważne przy niezbalansowanych danych.
