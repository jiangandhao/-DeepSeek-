"""肺结节检测演示(教学/毕设演示用)。

⚠️ 说明:为保证系统轻量可跑通(无需 GPU / 大模型),本模块采用
「高斯平滑 + 阈值分割 + 连通域分析 + 圆度筛选」的经典图像处理流程,
模拟肺结节候选检出与报告生成,演示「上传 → 检测 → 框选可视化 → 结构化报告」的完整链路。

工程升级路径(论文可论述):将 detect_nodules() 内部替换为基于 LUNA16 的
预训练 CNN(如 MONAI / torch)推理即可,接口与可视化、报告结构保持不变。
"""
import base64
import io

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

MAX_SIDE = 480          # 处理时缩放上限
MIN_AREA = 20           # 候选最小面积(像素)
MAX_AREA = 2500         # 候选最大面积
THRESHOLD_K = 1.6       # 阈值 = mean + k*std


def _to_gray_array(img: Image.Image) -> tuple[Image.Image, np.ndarray]:
    img = img.convert("L")
    w, h = img.size
    scale = min(MAX_SIDE / max(w, h), 1.0)
    if scale < 1.0:
        img = img.resize((int(w * scale), int(h * scale)))
    blurred = img.filter(ImageFilter.GaussianBlur(radius=1.5))
    return img, np.asarray(blurred, dtype=np.float32)


def _label_components(mask: np.ndarray) -> list[list[tuple[int, int]]]:
    """BFS 连通域标记(4 邻域),返回每个连通域的像素坐标列表。"""
    h, w = mask.shape
    visited = np.zeros_like(mask, dtype=bool)
    comps: list[list[tuple[int, int]]] = []
    ys, xs = np.nonzero(mask)
    for y0, x0 in zip(ys, xs):
        if visited[y0, x0]:
            continue
        stack = [(y0, x0)]
        visited[y0, x0] = True
        comp = []
        while stack:
            y, x = stack.pop()
            comp.append((y, x))
            for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                ny, nx = y + dy, x + dx
                if 0 <= ny < h and 0 <= nx < w and not visited[ny, nx] and mask[ny, nx]:
                    visited[ny, nx] = True
                    stack.append((ny, nx))
        comps.append(comp)
    return comps


def detect_nodules(image_bytes: bytes) -> dict:
    img = Image.open(io.BytesIO(image_bytes))
    gray_img, arr = _to_gray_array(img)
    h, w = arr.shape

    mean, std = float(arr.mean()), float(arr.std())
    threshold = mean + THRESHOLD_K * std
    mask = arr > threshold

    candidates = []
    for comp in _label_components(mask):
        area = len(comp)
        if area < MIN_AREA or area > MAX_AREA:
            continue
        ys = [p[0] for p in comp]
        xs = [p[1] for p in comp]
        y0, y1, x0, x1 = min(ys), max(ys), min(xs), max(xs)
        bw, bh = x1 - x0 + 1, y1 - y0 + 1
        if bw == 0 or bh == 0:
            continue
        fill = area / (bw * bh)            # 充实度
        aspect = min(bw, bh) / max(bw, bh)  # 长宽比(越接近1越圆)
        # 圆度筛选:结节多为类圆形
        if fill < 0.45 or aspect < 0.5:
            continue
        intensity = float(np.mean([arr[y, x] for y, x in comp])) / 255.0
        circularity = (fill + aspect) / 2
        confidence = round(float(min(0.4 + 0.35 * circularity + 0.25 * intensity, 0.97)), 2)
        candidates.append({
            "x": int(x0), "y": int(y0), "w": int(bw), "h": int(bh),
            "diameter_px": int(max(bw, bh)),
            "confidence": confidence,
        })

    # 取置信度最高的前若干个
    candidates.sort(key=lambda c: c["confidence"], reverse=True)
    candidates = candidates[:8]

    # 可视化:在原图上画框
    annotated = gray_img.convert("RGB")
    draw = ImageDraw.Draw(annotated)
    for i, c in enumerate(candidates, 1):
        draw.rectangle([c["x"], c["y"], c["x"] + c["w"], c["y"] + c["h"]],
                       outline=(255, 60, 60), width=2)
        draw.text((c["x"], max(c["y"] - 12, 0)), f"#{i} {c['confidence']}", fill=(255, 60, 60))
    buf = io.BytesIO()
    annotated.save(buf, format="PNG")
    annotated_b64 = "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()

    # 报告
    n = len(candidates)
    if n == 0:
        summary = "未检出明显结节候选(演示算法)。建议结合临床与专业影像诊断。"
        level = "LOW"
    else:
        max_d = max(c["diameter_px"] for c in candidates)
        level = "HIGH" if max_d >= 40 else "MEDIUM" if n >= 3 or max_d >= 20 else "LOW"
        summary = (f"检出 {n} 处结节候选区域,最大直径约 {max_d}px。"
                   f"风险提示:{ {'LOW':'低','MEDIUM':'中','HIGH':'高'}[level] }。"
                   "本结果为演示算法输出,需由放射科医师结合临床确诊。")

    return {
        "image_size": {"width": w, "height": h},
        "count": n,
        "level": level,
        "candidates": candidates,
        "summary": summary,
        "annotated_image": annotated_b64,
        "note": "演示用经典图像处理算法,非临床诊断;可替换为 LUNA16 预训练 CNN。",
    }
