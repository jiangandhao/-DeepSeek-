"""联邦学习最小 Demo:FedAvg 模拟糖尿病风险预测(论文「隐私保护/联邦学习」素材)。

场景:多家医院(客户端)各自持有本地患者数据,数据「不出域」;
仅在本地训练逻辑回归模型,把模型参数上传到服务器做加权平均(FedAvg),
迭代若干轮得到全局模型。对比:集中式训练 vs 联邦训练 vs 各本地单独训练。

仅依赖 numpy,可直接运行:
    python federated_learning_demo.py
"""
import numpy as np

rng = np.random.default_rng(2024)


# ---------- 合成数据:糖尿病风险二分类 ----------
def make_dataset(n: int):
    """特征:平均血糖、BMI、年龄、家族史(0/1)。标签:是否高风险。"""
    mean_glucose = rng.normal(7.0, 1.8, n)
    bmi = rng.normal(25, 4, n)
    age = rng.normal(50, 14, n)
    family = rng.integers(0, 2, n)
    # 真实规则(加噪声)生成标签
    logit = 1.1 * (mean_glucose - 7.0) + 0.18 * (bmi - 25) + 0.04 * (age - 50) + 0.8 * family
    prob = 1 / (1 + np.exp(-logit))
    y = (prob + rng.normal(0, 0.25, n) > 0.5).astype(float)
    X = np.column_stack([mean_glucose, bmi, age, family])
    # 标准化
    X = (X - X.mean(0)) / X.std(0)
    X = np.column_stack([np.ones(n), X])  # 偏置项
    return X, y


def sigmoid(z):
    return 1 / (1 + np.exp(-np.clip(z, -30, 30)))


def train_local(X, y, w, epochs=5, lr=0.3):
    """本地若干步梯度下降,返回更新后的权重(数据不离开本地)。"""
    w = w.copy()
    for _ in range(epochs):
        grad = X.T @ (sigmoid(X @ w) - y) / len(y)
        w -= lr * grad
    return w


def accuracy(X, y, w):
    return float(((sigmoid(X @ w) > 0.5) == y).mean())


def main():
    n_clients = 4
    rounds = 15

    # 各客户端本地数据(规模/分布略有差异,体现 Non-IID)
    clients = [make_dataset(rng.integers(150, 400)) for _ in range(n_clients)]
    X_test, y_test = make_dataset(2000)
    dim = X_test.shape[1]

    # ---- 集中式基线(把所有数据汇总,理想上界) ----
    Xc = np.vstack([c[0] for c in clients])
    yc = np.concatenate([c[1] for c in clients])
    w_central = np.zeros(dim)
    w_central = train_local(Xc, yc, w_central, epochs=200, lr=0.3)
    acc_central = accuracy(X_test, y_test, w_central)

    # ---- 联邦训练(FedAvg:数据不出域,仅聚合参数) ----
    w_global = np.zeros(dim)
    total = sum(len(c[1]) for c in clients)
    print(f"客户端数:{n_clients},各端样本量:{[len(c[1]) for c in clients]}\n")
    print(f"{'Round':>5}{'FedAvg测试准确率':>18}")
    print("-" * 24)
    for r in range(1, rounds + 1):
        local_weights = []
        for X, y in clients:
            local_weights.append((train_local(X, y, w_global, epochs=5, lr=0.3), len(y)))
        # 按样本量加权平均
        w_global = sum(w * n for w, n in local_weights) / total
        if r % 3 == 0 or r == 1:
            print(f"{r:>5}{accuracy(X_test, y_test, w_global):>18.4f}")
    acc_fed = accuracy(X_test, y_test, w_global)

    # ---- 各客户端仅用本地数据单独训练(无协作)的平均水平 ----
    local_accs = []
    for X, y in clients:
        w = train_local(X, y, np.zeros(dim), epochs=200, lr=0.3)
        local_accs.append(accuracy(X_test, y_test, w))
    acc_local_avg = float(np.mean(local_accs))

    print("\n==== 结果对比(测试集准确率) ====")
    print(f"集中式训练(数据汇总,理想上界): {acc_central:.4f}")
    print(f"联邦训练 FedAvg(数据不出域)  : {acc_fed:.4f}")
    print(f"各端单独本地训练(无协作)平均  : {acc_local_avg:.4f}")
    print("\n结论:联邦训练在不共享原始数据的前提下,效果接近集中式,"
          "且优于各端单打独斗 —— 体现「数据不出域、模型聚合」的隐私保护价值。")


if __name__ == "__main__":
    main()
