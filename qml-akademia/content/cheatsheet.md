## Kubit i pomiar
$$|\psi\rangle=\alpha|0\rangle+\beta|1\rangle,\qquad |\alpha|^2+|\beta|^2=1$$
$$|\psi\rangle=\cos\tfrac{\theta}{2}|0\rangle+e^{i\varphi}\sin\tfrac{\theta}{2}|1\rangle,\qquad \vec r=(\sin\theta\cos\varphi,\ \sin\theta\sin\varphi,\ \cos\theta)$$
- reguła Borna: $p(i)=|\langle i|\psi\rangle|^2$
- wartość oczekiwana: $\langle O\rangle=\langle\psi|O|\psi\rangle=\mathrm{Tr}(\rho O)$
- $\langle Z\rangle=p(0)-p(1)$; błąd estymaty z $N$ shotów $\sim1/\sqrt N$
- $|\pm\rangle=\frac{1}{\sqrt2}(|0\rangle\pm|1\rangle)$, $|\pm i\rangle=\frac{1}{\sqrt2}(|0\rangle\pm i|1\rangle)$
- macierz gęstości kubitu: $\rho=\frac12(I+\vec r\cdot\vec\sigma)$; czystość $\mathrm{Tr}\rho^2$

## Bramki
$$X=\begin{pmatrix}0&1\\1&0\end{pmatrix},\ Y=\begin{pmatrix}0&-i\\i&0\end{pmatrix},\ Z=\begin{pmatrix}1&0\\0&-1\end{pmatrix},\ H=\tfrac{1}{\sqrt2}\begin{pmatrix}1&1\\1&-1\end{pmatrix}$$
$$S=\mathrm{diag}(1,i),\quad T=\mathrm{diag}(1,e^{i\pi/4}),\quad R_\sigma(\theta)=e^{-i\theta\sigma/2}=\cos\tfrac{\theta}{2}I-i\sin\tfrac{\theta}{2}\sigma$$
$$R_Y(\theta)=\begin{pmatrix}\cos\frac{\theta}{2}&-\sin\frac{\theta}{2}\\ \sin\frac{\theta}{2}&\cos\frac{\theta}{2}\end{pmatrix},\qquad R_Z(\theta)=\begin{pmatrix}e^{-i\theta/2}&0\\0&e^{i\theta/2}\end{pmatrix}$$
- $HZH=X$, $HXH=Z$, $T^2=S$, $S^2=Z$, $XZ=-ZX$
- CNOT: $|c,t\rangle\mapsto|c,t\oplus c\rangle$; CZ $=\mathrm{diag}(1,1,1,-1)$; SWAP = 3×CNOT
- uniwersalne: $\{H,T,\mathrm{CNOT}\}$; Clifford $\{H,S,\mathrm{CNOT}\}$ — symulowalny klasycznie

## Wiele kubitów i splątanie
- wymiar $2^n$; $H^{\otimes n}|0\rangle^{\otimes n}=\frac{1}{\sqrt{2^n}}\sum_x|x\rangle$
- stany Bella: $|\Phi^\pm\rangle=\frac{|00\rangle\pm|11\rangle}{\sqrt2}$, $|\Psi^\pm\rangle=\frac{|01\rangle\pm|10\rangle}{\sqrt2}$
- entropia splątania: $S(\rho_A)=-\mathrm{Tr}\,\rho_A\log\rho_A$, $\rho_A=\mathrm{Tr}_B\rho_{AB}$
- swap test: $p(0)=\frac12+\frac12|\langle\phi|\psi\rangle|^2$

## Algorytmy
| Algorytm | Problem | Zapytania / koszt |
|---|---|---|
| Deutsch–Jozsa | stała vs zrównoważona | 1 (klasycznie det. $2^{n-1}+1$) |
| Bernstein–Vazirani | ukryte $s$ w $s\cdot x$ | 1 (klasycznie $n$) |
| Simon | ukryty okres XOR | $O(n)$ (klasycznie $\Omega(2^{n/2})$) |
| Grover | przeszukiwanie | $\approx\frac{\pi}{4}\sqrt{N/M}$ |
| Shor | faktoryzacja | wielomianowo, $O(n^3)$ |
| QFT | transformata Fouriera | $O(n^2)$ bramek |

$$\mathrm{QFT}|x\rangle=\frac{1}{\sqrt N}\sum_ke^{2\pi ixk/N}|k\rangle,\qquad U_f|x\rangle|-\rangle=(-1)^{f(x)}|x\rangle|-\rangle$$
- Shor: $a^r\equiv1\ (\mathrm{mod}\ N)$, dzielniki $\gcd(a^{r/2}\pm1,N)$
- VQE: $E(\boldsymbol\theta)=\langle\psi(\boldsymbol\theta)|H|\psi(\boldsymbol\theta)\rangle\ge E_0$
- QAOA: $\prod_le^{-i\beta_lH_M}e^{-i\gamma_lH_C}|+\rangle^{\otimes n}$, MaxCut: $H_C=\sum_{(i,j)}\frac12(1-Z_iZ_j)$

## QML
$$f_{\boldsymbol\theta}(\mathbf x)=\langle0|U^\dagger(\mathbf x,\boldsymbol\theta)\,M\,U(\mathbf x,\boldsymbol\theta)|0\rangle=\mathrm{Tr}\big(M_{\boldsymbol\theta}\rho(\mathbf x)\big)$$
$$k(\mathbf x,\mathbf x')=|\langle\phi(\mathbf x)|\phi(\mathbf x')\rangle|^2,\qquad \frac{\partial f}{\partial\theta}=\frac{f(\theta+\frac{\pi}{2})-f(\theta-\frac{\pi}{2})}{2}$$
- kodowanie kątowe: $\bigotimes_jR_Y(x_j)|0\rangle$; amplitudowe: $\frac{1}{\|\mathbf x\|}\sum_ix_i|i\rangle$ ($\log_2N$ kubitów)
- model z $L$ kodowaniami Pauliego: szereg Fouriera z częstotliwościami $\{-L,\dots,L\}$
- koszt gradientu: parameter‑shift $2P$ obwodów, SPSA 2 obwody, macierz Grama $m(m-1)/2$ obwodów
- barren plateaus: $\mathrm{Var}[\partial_k\mathcal L]\in O(b^{-n})$
- generalizacja: $\tilde O(\sqrt{T/N})$
- $p(y{=}1)=\frac{1-\langle Z\rangle}{2}$

## Klasyczne ML
- regresja liniowa: $\mathbf w^*=(X^\top X)^{-1}X^\top\mathbf y$; ridge: $(X^\top X+\lambda I)^{-1}X^\top\mathbf y$
- sigmoida $\sigma(z)=\frac{1}{1+e^{-z}}$; softmax $\frac{e^{z_k}}{\sum_je^{z_j}}$
- entropia krzyżowa: $-[y\log p+(1-y)\log(1-p)]$; gradient $(p-y)\mathbf x$
- GD: $\boldsymbol\theta\leftarrow\boldsymbol\theta-\eta\nabla\mathcal L$; Adam: $\theta\leftarrow\theta-\eta\,\hat m_t/(\sqrt{\hat v_t}+\epsilon)$
- bias–variance: $\mathbb E[(y-\hat f)^2]=\mathrm{Bias}^2+\mathrm{Var}+\sigma^2$
- SVM: $\min\frac12\|\mathbf w\|^2+C\sum_i\xi_i$; hinge $\max(0,1-yf(\mathbf x))$; RBF $e^{-\gamma\|\mathbf x-\mathbf x'\|^2}$
- Gini $1-\sum_kp_k^2$; entropia $-\sum_kp_k\log_2p_k$
- PCA: $\Sigma\mathbf v=\lambda\mathbf v$, zachowana wariancja $\sum_{i\le k}\lambda_i/\sum_i\lambda_i$
- uwaga: $\mathrm{softmax}(QK^\top/\sqrt{d_k})V$

## Metryki
| Metryka | Wzór |
|---|---|
| dokładność | $\frac{TP+TN}{\text{wszystkie}}$ |
| precyzja | $\frac{TP}{TP+FP}$ |
| czułość (recall) | $\frac{TP}{TP+FN}$ |
| F1 | $\frac{2PR}{P+R}$ |
| $R^2$ | $1-\frac{\sum(y_i-\hat y_i)^2}{\sum(y_i-\bar y)^2}$ |
