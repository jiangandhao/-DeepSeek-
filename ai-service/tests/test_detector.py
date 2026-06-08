import io

import numpy as np
from PIL import Image

from app.imaging.detector import detect_nodules


def _synthetic_ct(blobs):
    rng = np.random.default_rng(5)
    img = rng.normal(40, 12, (300, 300)).clip(0, 110).astype("uint8")
    yy, xx = np.ogrid[:300, :300]
    for cx, cy, r, val in blobs:
        img[(xx - cx) ** 2 + (yy - cy) ** 2 <= r * r] = val
    buf = io.BytesIO()
    Image.fromarray(img).save(buf, format="PNG")
    return buf.getvalue()


def test_detect_finds_blobs():
    img = _synthetic_ct([(90, 100, 10, 225), (200, 160, 14, 235)])
    res = detect_nodules(img)
    assert res["count"] >= 1
    assert res["level"] in ("LOW", "MEDIUM", "HIGH")
    assert res["annotated_image"].startswith("data:image/png;base64,")
    for c in res["candidates"]:
        assert {"x", "y", "w", "h", "confidence"} <= set(c)
        assert 0 <= c["confidence"] <= 1


def test_detect_confidence_is_plain_float():
    img = _synthetic_ct([(150, 150, 12, 230)])
    res = detect_nodules(img)
    if res["candidates"]:
        assert type(res["candidates"][0]["confidence"]) is float


def test_blank_image_few_or_no_nodules():
    rng = np.random.default_rng(1)
    flat = (np.ones((200, 200)) * 30).astype("uint8")
    buf = io.BytesIO()
    Image.fromarray(flat).save(buf, format="PNG")
    res = detect_nodules(buf.getvalue())
    assert res["count"] == 0
    assert res["level"] == "LOW"
