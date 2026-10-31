---
id: ml-intro
title: Czym jest uczenie maszynowe?
summary: Rodzaje uczenia, dane, cechy, etykiety i podział na zbiory.
level: podstawowy
---
**Uczenie maszynowe** (ML) to budowanie modeli, które uczą się zależności z danych, zamiast być jawnie zaprogramowane. Formalnie szukamy funkcji $f_{\boldsymbol\theta}$ z rodziny hipotez, która minimalizuje **ryzyko empiryczne**
$$\hat R(\boldsymbol\theta)=\frac1m\sum_{i=1}^{m}\ell\big(f_{\boldsymbol\theta}(\mathbf x_i),y_i\big).$$

### Rodzaje uczenia
- **nadzorowane** (*supervised*): dane $(\mathbf x,y)$ z etykietami — klasyfikacja (etykieta dyskretna) i regresja (ciągła),
- **nienadzorowane**: tylko $\mathbf x$ — klasteryzacja, redukcja wymiaru, estymacja gęstości, modele generatywne,
- **samonadzorowane** (*self‑supervised*): etykiety tworzone z samych danych (np. przewidywanie następnego słowa w modelach językowych),
- **ze wzmocnieniem** (RL): agent uczy się polityki z nagród w środowisku,
- **półnadzorowane**: mało etykiet + dużo danych bez etykiet.

### Słowniczek
- **cecha** (*feature*) — zmienna wejściowa $x_j$; **wektor cech** $\mathbf x\in\mathbb R^d$,
- **etykieta** (*label, target*) $y$,
- **model** — rodzina funkcji z parametrami; **hiperparametry** — ustawienia, których nie uczy się gradientem (np. krok uczenia, głębokość drzewa),
- **funkcja straty** $\ell$ — kara za błąd na jednym przykładzie.

### Podział danych
- **treningowy** — do dopasowania parametrów,
- **walidacyjny** — do wyboru hiperparametrów i modelu,
- **testowy** — tylko do końcowej oceny (jednorazowo!).

Typowo 60/20/20 lub 80/10/10; przy małych danych — **walidacja krzyżowa**.

### Generalizacja
Celem jest małe **ryzyko oczekiwane** na nowych danych z tego samego rozkładu (założenie i.i.d.), a nie zapamiętanie danych treningowych. Różnica między błędem testowym a treningowym to **luka generalizacji**.

> [!warn] Wyciek danych (data leakage)
> Jeśli informacja ze zbioru testowego „przecieknie” do treningu (np. normalizacja liczona na wszystkich danych, duplikaty, cechy z przyszłości), wyniki będą zawyżone.

@@@ fiszki
Q: Czym różni się uczenie nadzorowane od nienadzorowanego?
A: Nadzorowane wykorzystuje pary $(\mathbf x,y)$ z etykietami; nienadzorowane — tylko $\mathbf x$ (szuka struktury: klastrów, niskiego wymiaru, gęstości).

Q: Co to jest ryzyko empiryczne?
A: Średnia strata na zbiorze treningowym: $\frac1m\sum_i\ell(f(\mathbf x_i),y_i)$.

Q: Do czego służy zbiór walidacyjny, a do czego testowy?
A: Walidacyjny — do wyboru hiperparametrów i modelu; testowy — do jednorazowej, końcowej oceny generalizacji.

Q: Czym są hiperparametry?
A: Ustawieniami modelu lub treningu, których nie wyznacza optymalizacja parametrów (np. krok uczenia, liczba warstw, $C$ w SVM).

Q: Co to jest wyciek danych (data leakage)?
A: Przedostanie się informacji ze zbioru testowego (lub z przyszłości) do treningu, co zawyża wyniki.

Q: Czym jest uczenie samonadzorowane?
A: Uczeniem, w którym etykiety generuje się z samych danych (np. maskowane słowa, następny token).

@@@ quiz
? Przewidywanie ceny mieszkania na podstawie metrażu to:
+ regresja (uczenie nadzorowane)
- klasyfikacja nienadzorowana
- klasteryzacja
- uczenie ze wzmocnieniem
! Etykieta jest ciągła → regresja.

? Kiedy wolno używać zbioru testowego?
- Do strojenia kroku uczenia
- Do wczesnego zatrzymania
+ Tylko do końcowej oceny wybranego modelu
- Do normalizacji cech
! Każde inne użycie daje optymistycznie obciążone wyniki.

? Który przykład to wyciek danych?
+ Standaryzacja cech ze średnią i odchyleniem policzonymi na całym zbiorze przed podziałem
- Użycie walidacji krzyżowej
- Losowe tasowanie przed podziałem (bez duplikatów)
- Regularyzacja L2
! Statystyki do preprocessingu liczy się wyłącznie na zbiorze treningowym.
%%%
---
id: ml-linreg
title: Regresja liniowa i funkcje straty
summary: Model liniowy, MSE, równanie normalne i interpretacja geometryczna.
level: podstawowy
---
### Model
$$\hat y=\mathbf w^\top\mathbf x+b,\qquad \text{macierzowo: } \hat{\mathbf y}=X\mathbf w$$
(kolumna jedynek w $X$ „pochłania” wyraz wolny $b$).

### Strata MSE
$$\mathcal L(\mathbf w)=\frac1m\|X\mathbf w-\mathbf y\|^2=\frac1m\sum_i(\hat y_i-y_i)^2$$
MSE odpowiada **maksymalnej wiarygodności** przy założeniu szumu gaussowskiego. Jest wypukła — każde minimum lokalne jest globalne.

### Rozwiązanie analityczne (równanie normalne)
$$\nabla\mathcal L=\tfrac2mX^\top(X\mathbf w-\mathbf y)=0\quad\Rightarrow\quad \mathbf w^*=(X^\top X)^{-1}X^\top\mathbf y.$$
Koszt $O(md^2+d^3)$ — dla dużego $d$ lepszy jest spadek gradientu. Gdy $X^\top X$ jest osobliwa (współliniowe cechy), używa się pseudoodwrotności albo regularyzacji (ridge: $(X^\top X+\lambda I)^{-1}X^\top\mathbf y$).

Geometrycznie: $X\mathbf w^*$ to **rzut ortogonalny** wektora $\mathbf y$ na przestrzeń kolumn $X$.

### Inne straty i miary regresji
- **MAE** $\frac1m\sum|\hat y_i-y_i|$ — odporna na wartości odstające (odpowiada medianie),
- **Huber** — kwadratowa dla małych błędów, liniowa dla dużych,
- **$R^2=1-\frac{\sum(y_i-\hat y_i)^2}{\sum(y_i-\bar y)^2}$** — jaka część wariancji $y$ jest wyjaśniona.

### Nieliniowość przez cechy
Regresja „liniowa” jest liniowa w **parametrach**, nie w danych: cechy wielomianowe $x, x^2, x^3$ czy inne funkcje bazowe dają krzywe dopasowania. To ta sama idea co mapy cech w metodach jądrowych i w QML.

@@@ fiszki
Q: Zapisz równanie normalne regresji liniowej.
A: $\mathbf w^*=(X^\top X)^{-1}X^\top\mathbf y$.

Q: Przy jakim założeniu o szumie MSE odpowiada maksymalnej wiarygodności?
A: Przy szumie gaussowskim o stałej wariancji.

Q: Kiedy MAE jest lepsza od MSE?
A: Gdy w danych są wartości odstające — MAE karze je liniowo, a nie kwadratowo.

Q: Jak interpretować $R^2$?
A: Jako część wariancji $y$ wyjaśnioną przez model; $R^2=1$ — idealne dopasowanie, $0$ — tyle co przewidywanie średniej.

Q: Dlaczego regresję z cechami $x, x^2$ nadal nazywamy liniową?
A: Bo model jest liniowy w parametrach (wagach), choć nieliniowy względem $x$.

@@@ quiz
? Jaka jest geometryczna interpretacja rozwiązania metodą najmniejszych kwadratów?
+ Rzut ortogonalny wektora $\mathbf y$ na przestrzeń kolumn macierzy $X$
- Obrót wektora $\mathbf y$
- Odbicie $\mathbf y$ względem średniej
- Najdłuższy wektor w przestrzeni wierszy
! Wektor reszt jest ortogonalny do kolumn $X$.

? Model zawsze przewiduje średnią $\bar y$. Ile wynosi $R^2$?
- 1
+ 0
- −1
- 0.5
! Licznik i mianownik ułamka są równe.

? Co zrobić, gdy $X^\top X$ jest osobliwa?
- Usunąć zbiór testowy
+ Użyć regularyzacji ridge lub pseudoodwrotności
- Zwiększyć krok uczenia
- Zamienić MSE na dokładność
! $X^\top X+\lambda I$ jest odwracalna dla $\lambda>0$.
%%%
---
id: ml-logreg
title: Klasyfikacja: regresja logistyczna i softmax
summary: Sigmoida, entropia krzyżowa, granica decyzyjna i klasyfikacja wieloklasowa.
level: podstawowy
lab: train
---
### Regresja logistyczna
Model prawdopodobieństwa klasy 1:
$$p(y{=}1\mid\mathbf x)=\sigma(\mathbf w^\top\mathbf x+b),\qquad \sigma(z)=\frac{1}{1+e^{-z}}.$$
Logarytm szans (*log‑odds*) jest liniowy: $\log\frac{p}{1-p}=\mathbf w^\top\mathbf x+b$.

### Binarna entropia krzyżowa (log loss)
$$\ell=-\big[y\log p+(1-y)\log(1-p)\big]$$
Wynika z maksymalnej wiarygodności dla rozkładu Bernoulliego. Gradient jest elegancki:
$$\nabla_{\mathbf w}\ell=(p-y)\,\mathbf x.$$
Strata jest wypukła względem $\mathbf w$, ale nie ma rozwiązania analitycznego — używa się spadku gradientu albo metody Newtona (IRLS).

### Granica decyzyjna
Klasa 1, gdy $p>0.5\iff\mathbf w^\top\mathbf x+b>0$ — granica to **hiperpłaszczyzna**. Regresja logistyczna nie rozdzieli danych typu XOR czy „okrąg” bez nieliniowych cech (zobacz Lab!).

### Wiele klas: softmax
$$p(y{=}k\mid\mathbf x)=\frac{e^{z_k}}{\sum_je^{z_j}},\qquad z_k=\mathbf w_k^\top\mathbf x+b_k,$$
ze stratą **kategorycznej entropii krzyżowej** $-\log p(y{=}\text{prawdziwa klasa})$. Alternatywa: one‑vs‑rest ($K$ klasyfikatorów binarnych).

### Kalibracja i próg
Wyjście to prawdopodobieństwo — można zmieniać próg decyzji (np. 0.3 w diagnostyce, by zwiększyć czułość). Model jest dobrze **skalibrowany**, jeśli wśród przypadków z $p\approx0.8$ ok. 80% to rzeczywiście klasa 1.

@@@ fiszki
Q: Zapisz model regresji logistycznej.
A: $p(y{=}1|\mathbf x)=\sigma(\mathbf w^\top\mathbf x+b)$, gdzie $\sigma(z)=1/(1+e^{-z})$.

Q: Jaki jest gradient entropii krzyżowej regresji logistycznej względem wag?
A: $(p-y)\mathbf x$.

Q: Jaki kształt ma granica decyzyjna regresji logistycznej?
A: Hiperpłaszczyzny $\mathbf w^\top\mathbf x+b=0$ (granica liniowa).

Q: Jak działa funkcja softmax?
A: Zamienia wektor logitów $z$ na rozkład prawdopodobieństwa: $e^{z_k}/\sum_je^{z_j}$.

Q: Co to znaczy, że klasyfikator jest skalibrowany?
A: Przewidywane prawdopodobieństwa zgadzają się z częstościami (np. 80% przypadków z $p=0.8$ to klasa pozytywna).

@@@ quiz
? Ile wynosi $\sigma(0)$?
- 0
+ 0.5
- 1
- $e$
! $1/(1+e^0)=1/2$.

? Dlaczego regresja logistyczna nie rozwiąże XOR na surowych cechach $(x_1,x_2)$?
+ Bo jej granica decyzyjna jest liniowa, a XOR nie jest liniowo separowalny
- Bo sigmoida nie jest różniczkowalna
- Bo XOR ma za mało danych
- Bo entropia krzyżowa nie jest wypukła
! Potrzebna jest np. cecha $x_1x_2$ albo model nieliniowy.

? Która strata odpowiada maksymalnej wiarygodności w klasyfikacji binarnej?
- MSE
+ Binarna entropia krzyżowa
- Hinge
- MAE
! Rozkład Bernoulliego → log loss.
%%%
---
id: ml-gd
title: Optymalizacja: spadek gradientu, SGD, Adam
summary: Jak modele się uczą — krok uczenia, mini‑batche, momentum i adaptacyjne optymalizatory.
level: podstawowy
---
### Spadek gradientu
$$\boldsymbol\theta_{t+1}=\boldsymbol\theta_t-\eta\nabla\mathcal L(\boldsymbol\theta_t)$$
$\eta$ to **krok uczenia** (*learning rate*): za mały → powolna nauka; za duży → oscylacje albo rozbieżność. Dla funkcji wypukłych z $L$‑lipschitzowskim gradientem zbieżność gwarantuje $\eta\le1/L$.

### Warianty
- **batch GD** — gradient z całego zbioru (dokładny, drogi),
- **SGD** — z jednego przykładu (szybki, zaszumiony),
- **mini‑batch SGD** — z $B$ przykładów (standard: $B=32$–$512$); szum działa jak regularyzacja i pomaga uciekać z punktów siodłowych.

### Momentum i adaptacja
- **Momentum**: $\mathbf v\leftarrow\beta\mathbf v+\nabla\mathcal L$, $\boldsymbol\theta\leftarrow\boldsymbol\theta-\eta\mathbf v$ — wygładza kierunek, przyspiesza w „dolinach”,
- **AdaGrad / RMSProp** — krok dzielony przez średnią kwadratów gradientów (osobno dla każdego parametru),
- **Adam** = momentum + RMSProp z korekcją obciążenia:
$$m_t=\beta_1m_{t-1}+(1-\beta_1)g_t,\quad v_t=\beta_2v_{t-1}+(1-\beta_2)g_t^2,\quad \theta\leftarrow\theta-\eta\frac{\hat m_t}{\sqrt{\hat v_t}+\epsilon}$$
(domyślnie $\beta_1=0.9$, $\beta_2=0.999$). **AdamW** oddziela od tego weight decay.

### Harmonogram kroku
Stały, malejący (skokowo lub wykładniczo), **cosine annealing**, **warm‑up** (popularny w transformerach).

### Krajobraz straty
Sieci neuronowe mają niewypukłe funkcje straty: minima lokalne, **punkty siodłowe** (w wysokim wymiarze dominują) i płaskie obszary. Szerokie (płaskie) minima zwykle lepiej generalizują.

> [!info] Związek z QML
> W obwodach wariacyjnych gradient liczy się regułą parameter‑shift, a szum shotów dodaje „naturalną stochastyczność” — Adam i SPSA to najczęstsze wybory.

@@@ fiszki
Q: Zapisz regułę aktualizacji spadku gradientu.
A: $\boldsymbol\theta\leftarrow\boldsymbol\theta-\eta\nabla\mathcal L(\boldsymbol\theta)$.

Q: Co się dzieje przy zbyt dużym kroku uczenia?
A: Oscylacje wokół minimum albo rozbieżność (strata rośnie).

Q: Czym mini‑batch SGD różni się od batch GD?
A: Gradient liczy się na losowej podgrupie $B$ przykładów — taniej i z szumem, który może pomagać w generalizacji.

Q: Jakie dwa mechanizmy łączy Adam?
A: Momentum (średnia gradientów $m_t$) i adaptacyjny krok RMSProp (średnia kwadratów $v_t$), z korekcją obciążenia.

Q: Czym jest warm‑up kroku uczenia?
A: Stopniowym zwiększaniem kroku na początku treningu, by ustabilizować optymalizację (częste w transformerach).

@@@ quiz
? Strata podczas treningu gwałtownie rośnie i oscyluje. Najbardziej prawdopodobna przyczyna to:
+ Zbyt duży krok uczenia
- Zbyt mały krok uczenia
- Zbyt duży zbiór testowy
- Za mało epok
! Duże kroki „przeskakują” minimum.

? Jakie są domyślne wartości $\beta_1$ i $\beta_2$ w Adamie?
+ 0.9 i 0.999
- 0.5 i 0.5
- 0.99 i 0.9
- 0.1 i 0.01
! Zalecane w oryginalnej pracy Kingmy i Ba (2015).

? Co dominuje w krajobrazie straty w wysokim wymiarze?
- Minima globalne
+ Punkty siodłowe
- Maksima lokalne
- Nieciągłości
! Prawdopodobieństwo, że wszystkie kierunki prowadzą „w górę”, maleje z wymiarem.
%%%
---
id: ml-overfit
title: Przeuczenie, regularyzacja i walidacja
summary: Kompromis obciążenie–wariancja, L1/L2, dropout, early stopping i walidacja krzyżowa.
level: podstawowy
---
### Niedouczenie i przeuczenie
- **niedouczenie** (*underfitting*): model za prosty — duży błąd i na treningu, i na teście,
- **przeuczenie** (*overfitting*): model zapamiętuje szum — mały błąd treningowy, duży testowy.

### Kompromis obciążenie–wariancja
Dla MSE oczekiwany błąd rozkłada się na:
$$\mathbb E[(y-\hat f)^2]=\underbrace{\mathrm{Bias}[\hat f]^2}_{\text{za prosty model}}+\underbrace{\mathrm{Var}[\hat f]}_{\text{wrażliwość na dane}}+\underbrace{\sigma^2}_{\text{szum nieredukowalny}}.$$
W głębokim uczeniu obserwuje się też **double descent**: po przekroczeniu progu interpolacji błąd testowy znów maleje.

### Regularyzacja
- **L2 (ridge, weight decay)**: $+\lambda\|\mathbf w\|_2^2$ — małe, rozproszone wagi,
- **L1 (lasso)**: $+\lambda\|\mathbf w\|_1$ — wagi **rzadkie** (selekcja cech),
- **elastic net**: połączenie L1 i L2,
- **dropout**: losowe wyłączanie neuronów podczas treningu,
- **early stopping**: zatrzymanie, gdy błąd walidacyjny przestaje maleć,
- **augmentacja danych**, więcej danych, prostszy model.

### Walidacja krzyżowa (k‑fold)
Dzielimy dane na $k$ części; trenujemy na $k-1$, oceniamy na pozostałej i uśredniamy wyniki. Typowo $k=5$ lub $10$. Warianty: **stratified** (zachowane proporcje klas), **group**, **time series split** (bez „przyszłości” w treningu).

### Krzywe uczenia
Wykres błędu treningowego i walidacyjnego w funkcji liczby danych lub epok pozwala odróżnić wysokie obciążenie (oba błędy wysokie) od wysokiej wariancji (duża luka).

@@@ fiszki
Q: Jakie są objawy przeuczenia?
A: Niski błąd treningowy i wysoki błąd walidacyjny/testowy (duża luka generalizacji).

Q: Zapisz rozkład błędu na obciążenie i wariancję.
A: $\mathbb E[(y-\hat f)^2]=\mathrm{Bias}^2+\mathrm{Var}+\sigma^2$.

Q: Czym różni się regularyzacja L1 od L2?
A: L1 ($\|\mathbf w\|_1$) daje rzadkie wagi (dużo zer), L2 ($\|\mathbf w\|_2^2$) — małe, ale niezerowe wagi.

Q: Na czym polega walidacja krzyżowa k‑fold?
A: Dzielimy dane na $k$ części; każda raz jest walidacją, a reszta treningiem; wyniki uśredniamy.

Q: Czym jest early stopping?
A: Zatrzymaniem treningu, gdy błąd na zbiorze walidacyjnym przestaje się poprawiać.

Q: Co to jest double descent?
A: Zjawisko, w którym błąd testowy po przekroczeniu progu interpolacji (bardzo duże modele) ponownie maleje.

@@@ quiz
? Model ma 99% dokładności na treningu i 70% na teście. Diagnoza:
+ Przeuczenie (wysoka wariancja)
- Niedouczenie (wysokie obciążenie)
- Idealny model
- Wyciek danych z testu do treningu
! Duża luka generalizacji.

? Która regularyzacja prowadzi do rzadkich wag (wielu zer)?
+ L1 (lasso)
- L2 (ridge)
- Dropout
- Batch normalization
! Kształt normy L1 sprzyja rozwiązaniom leżącym na osiach.

? Którego podziału użyć dla szeregów czasowych?
- Losowego k‑fold
+ Time series split (trening zawsze wcześniej w czasie niż walidacja)
- Losowego leave‑one‑out
- Bootstrapu bez ograniczeń
! Losowy podział przemyca informację z przyszłości.

? Model jest za prosty — błąd jest wysoki i na treningu, i na walidacji. Co pomoże?
+ Bardziej złożony model lub lepsze cechy
- Silniejsza regularyzacja
- Wcześniejsze zatrzymanie
- Mniej cech
! To niedouczenie (wysokie obciążenie).
%%%
---
id: ml-metrics
title: Metryki oceny modeli
summary: Macierz pomyłek, precyzja, czułość, F1, ROC‑AUC i metryki regresji.
level: podstawowy
---
### Macierz pomyłek (klasyfikacja binarna)
| | przewidziano + | przewidziano − |
|---|---|---|
| **faktycznie +** | TP | FN |
| **faktycznie −** | FP | TN |

### Metryki
- **dokładność** (*accuracy*) $=\frac{TP+TN}{TP+TN+FP+FN}$ — myląca przy niezbalansowanych klasach,
- **precyzja** (*precision*) $=\frac{TP}{TP+FP}$ — jaka część wykrytych jest prawdziwa,
- **czułość / pełność** (*recall*, TPR) $=\frac{TP}{TP+FN}$ — jaką część prawdziwych wykryliśmy,
- **swoistość** (*specificity*) $=\frac{TN}{TN+FP}$,
- **F1** $=2\cdot\frac{P\cdot R}{P+R}$ — średnia harmoniczna precyzji i czułości.

### Krzywe
- **ROC**: TPR w funkcji FPR przy zmianie progu; **AUC** to prawdopodobieństwo, że losowy przykład pozytywny dostanie wyższy wynik niż losowy negatywny (0.5 — losowo, 1 — idealnie),
- **Precision–Recall** (i AP) — lepsza przy rzadkiej klasie pozytywnej.

### Niezbalansowane dane
Przy 1% klasy pozytywnej model „zawsze −” ma 99% dokładności i zerową użyteczność. Używaj F1, PR‑AUC, balanced accuracy, wag klas albo resamplingu (np. SMOTE).

### Wiele klas
Uśrednianie **macro** (średnia po klasach — każda klasa równie ważna), **micro** (sumy TP/FP/FN — każda próbka równie ważna), **weighted** (ważone liczebnością klas).

### Regresja
MSE, RMSE (w jednostkach $y$), MAE, $R^2$, MAPE.

> [!tip] Wybór metryki to wybór kosztu błędu
> Diagnostyka raka: ważna czułość (nie przeoczyć choroby). Filtr spamu: ważna precyzja (nie wyrzucić ważnej wiadomości).

@@@ fiszki
Q: Podaj wzory na precyzję i czułość.
A: Precyzja $=TP/(TP+FP)$, czułość $=TP/(TP+FN)$.

Q: Co to jest F1?
A: Średnia harmoniczna precyzji i czułości: $2PR/(P+R)$.

Q: Jak interpretować ROC‑AUC?
A: Jako prawdopodobieństwo, że losowy przykład pozytywny ma wyższy wynik niż losowy negatywny; 0.5 oznacza klasyfikator losowy.

Q: Dlaczego dokładność bywa myląca?
A: Przy niezbalansowanych klasach trywialny model (zawsze klasa większościowa) ma wysoką dokładność.

Q: Czym różni się uśrednianie macro od micro?
A: Macro — średnia metryk po klasach (klasy równie ważne); micro — metryka z sumarycznych TP/FP/FN (próbki równie ważne).

@@@ quiz
? TP=40, FP=10, FN=20. Ile wynosi precyzja?
+ 0.8
- 0.67
- 0.5
- 0.4
! $40/(40+10)=0.8$.

? Te same dane: TP=40, FN=20. Ile wynosi czułość?
- 0.8
+ 0.67
- 0.33
- 0.5
! $40/60\approx0.67$.

? Która metryka jest najlepsza przy bardzo rzadkiej klasie pozytywnej?
- Dokładność
+ PR‑AUC (lub F1)
- MSE
- $R^2$
! Dokładność i ROC bywają zbyt optymistyczne przy dużej liczbie TN.

? AUC = 0.5 oznacza:
+ Model nie lepszy od losowego zgadywania
- Model idealny
- Model, który zawsze się myli
- Połowę danych testowych
! Krzywa ROC leży na przekątnej.
%%%
---
id: ml-svm
title: SVM i metody jądrowe
summary: Maksymalny margines, hinge loss, sztuczka jądrowa i twierdzenie Mercera — prosty pomost do kerneli kwantowych.
level: średni
lab: kernel
---
### Maksymalny margines
SVM szuka hiperpłaszczyzny $\mathbf w^\top\mathbf x+b=0$, która rozdziela klasy $y\in\{-1,+1\}$ z **największym marginesem** $2/\|\mathbf w\|$:
$$\min_{\mathbf w,b,\boldsymbol\xi}\ \tfrac12\|\mathbf w\|^2+C\sum_i\xi_i\quad\text{przy}\quad y_i(\mathbf w^\top\mathbf x_i+b)\ge1-\xi_i,\ \ \xi_i\ge0.$$
$C$ steruje kompromisem margines ↔ błędy (*soft margin*). Równoważnie: minimalizacja **hinge loss** $\max(0,1-yf(\mathbf x))$ z regularyzacją L2.

### Postać dualna i wektory nośne
$$\max_{\boldsymbol\alpha}\ \sum_i\alpha_i-\tfrac12\sum_{i,j}\alpha_i\alpha_jy_iy_j\,\mathbf x_i^\top\mathbf x_j,\qquad 0\le\alpha_i\le C,\quad \sum_i\alpha_iy_i=0.$$
Predykcja: $f(\mathbf x)=\sum_i\alpha_iy_i\,\mathbf x_i^\top\mathbf x+b$. Znaczenie mają tylko punkty z $\alpha_i>0$ (**wektory nośne**). Dane występują **wyłącznie przez iloczyny skalarne**!

### Sztuczka jądrowa (kernel trick)
Zastępujemy $\mathbf x_i^\top\mathbf x_j$ przez $k(\mathbf x_i,\mathbf x_j)=\langle\varphi(\mathbf x_i),\varphi(\mathbf x_j)\rangle$ — to liniowy SVM w (być może nieskończenie wymiarowej) przestrzeni cech, bez jawnego liczenia $\varphi$.
- liniowy: $\mathbf x^\top\mathbf x'$,
- wielomianowy: $(\mathbf x^\top\mathbf x'+c)^d$,
- **RBF (gaussowski)**: $\exp(-\gamma\|\mathbf x-\mathbf x'\|^2)$ — nieskończony wymiar cech; $\gamma$ to odwrotność szerokości pasma.

**Twierdzenie Mercera**: symetryczna funkcja $k$ jest kernelem (iloczynem skalarnym w pewnej przestrzeni cech) wtedy i tylko wtedy, gdy każda macierz Grama $K_{ij}=k(\mathbf x_i,\mathbf x_j)$ jest dodatnio półokreślona.

### Pomost do QML
Kernel kwantowy $|\langle\phi(\mathbf x)|\phi(\mathbf x')\rangle|^2$ jest poprawnym kernelem Mercera (iloczyn skalarny macierzy gęstości). QSVM = klasyczny SVM + kernel liczony na QPU. Wszystko, co wiesz o $\gamma$ w RBF (za duże → przeuczenie, $K\approx I$), przenosi się na **szerokość pasma** kerneli kwantowych.

### Wady
Macierz Grama $m\times m$ oznacza $O(m^2)$ pamięci i $O(m^2)$–$O(m^3)$ czasu — SVM słabo skaluje się do milionów próbek (stąd przybliżenia: metoda Nyströma, random Fourier features).

@@@ fiszki
Q: Co maksymalizuje SVM?
A: Margines $2/\|\mathbf w\|$ między hiperpłaszczyzną a najbliższymi punktami obu klas.

Q: Czym jest hinge loss?
A: $\max(0,1-y f(\mathbf x))$ — kara za punkty po złej stronie granicy lub wewnątrz marginesu.

Q: Co to są wektory nośne?
A: Punkty treningowe z $\alpha_i>0$ w postaci dualnej — tylko one wpływają na granicę decyzyjną.

Q: Na czym polega sztuczka jądrowa?
A: Na zastąpieniu iloczynu skalarnego $\mathbf x^\top\mathbf x'$ kernelem $k(\mathbf x,\mathbf x')=\langle\varphi(\mathbf x),\varphi(\mathbf x')\rangle$ bez jawnego liczenia cech.

Q: Co mówi twierdzenie Mercera?
A: Symetryczna funkcja jest kernelem wtedy i tylko wtedy, gdy wszystkie jej macierze Grama są dodatnio półokreślone.

Q: Zapisz kernel RBF.
A: $k(\mathbf x,\mathbf x')=\exp(-\gamma\|\mathbf x-\mathbf x'\|^2)$.

Q: Jak $C$ wpływa na SVM?
A: Duże $C$ — mało błędów treningowych, wąski margines (ryzyko przeuczenia); małe $C$ — szerszy margines i więcej tolerowanych błędów.

@@@ quiz
? W kernelu RBF bardzo duże $\gamma$ prowadzi do:
+ Przeuczenia — macierz Grama bliska jednostkowej
- Niedouczenia — wszystkie wartości kernela bliskie 1
- Liniowej granicy decyzyjnej
- Nieokreśloności macierzy Grama
! Każdy punkt jest „podobny” tylko do siebie.

? Które punkty decydują o granicy decyzyjnej SVM?
- Wszystkie punkty treningowe po równo
+ Wektory nośne
- Punkty najdalej od granicy
- Średnie klas
! W postaci dualnej liczą się tylko $\alpha_i>0$.

? Dlaczego kernel kwantowy $|\langle\phi(\mathbf x)|\phi(\mathbf x')\rangle|^2$ może być użyty w SVM?
+ Bo jest iloczynem skalarnym macierzy gęstości, więc macierz Grama jest dodatnio półokreślona
- Bo zawsze równa się kernelowi RBF
- Bo jest liczbą zespoloną
- Bo nie wymaga danych treningowych
! $\mathrm{Tr}(\rho\rho')$ to iloczyn skalarny Hilberta–Schmidta.

? Jaka jest złożoność pamięciowa przechowania macierzy Grama dla $m$ próbek?
- $O(m)$
+ $O(m^2)$
- $O(\log m)$
- $O(m^3)$
! To ogranicza skalowanie SVM (i QSVM).
