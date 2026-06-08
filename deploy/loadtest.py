"""轻量并发压测脚本(论文「高并发」实验佐证)。

对后端关键接口做并发压测,统计 QPS、成功率、延迟分位数(p50/p95/p99)。
仅依赖标准库,可直接运行:

    python loadtest.py                       # 默认登录+查血糖,各 500 请求,并发 50
    python loadtest.py --requests 1000 --concurrency 100
"""
import argparse
import json
import statistics
import time
import urllib.request
from concurrent.futures import ThreadPoolExecutor

BASE = "http://localhost:8080"


def login(user, pwd):
    data = json.dumps({"username": user, "password": pwd}).encode()
    req = urllib.request.Request(BASE + "/api/auth/login", data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())["data"]["token"]


def timed_get(path, token):
    req = urllib.request.Request(BASE + path, method="GET")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    t0 = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            r.read()
            ok = r.status == 200
    except Exception:
        ok = False
    return ok, (time.perf_counter() - t0) * 1000  # ms


def run_case(name, fn, total, concurrency):
    latencies, oks = [], 0
    start = time.perf_counter()
    with ThreadPoolExecutor(max_workers=concurrency) as ex:
        for ok, ms in ex.map(lambda _: fn(), range(total)):
            latencies.append(ms)
            oks += 1 if ok else 0
    elapsed = time.perf_counter() - start
    latencies.sort()

    def pct(p):
        return latencies[min(len(latencies) - 1, int(len(latencies) * p))]

    print(f"\n== {name} ==")
    print(f"  请求数 {total} | 并发 {concurrency} | 耗时 {elapsed:.2f}s")
    print(f"  QPS:        {total / elapsed:.1f}")
    print(f"  成功率:     {oks / total * 100:.1f}%")
    print(f"  延迟(ms):   p50 {pct(0.5):.1f} | p95 {pct(0.95):.1f} | p99 {pct(0.99):.1f} | avg {statistics.mean(latencies):.1f}")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--requests", type=int, default=500)
    ap.add_argument("--concurrency", type=int, default=50)
    ap.add_argument("--user", default="demo")
    ap.add_argument("--pwd", default="demo123")
    args = ap.parse_args()

    token = login(args.user, args.pwd)
    print(f"登录成功,开始压测 (并发 {args.concurrency})")

    run_case("查询血糖记录 GET /api/glucose", lambda: timed_get("/api/glucose", token),
             args.requests, args.concurrency)
    run_case("健康检查 GET /actuator/health", lambda: timed_get("/actuator/health", None),
             args.requests, args.concurrency)


if __name__ == "__main__":
    main()
