"""影像识别路由(肺结节检测演示)。"""
from fastapi import APIRouter, File, UploadFile

from app.imaging.detector import detect_nodules

router = APIRouter(prefix="/api/imaging", tags=["imaging"])


@router.post("/detect")
async def detect(file: UploadFile = File(...)):
    """上传 CT 切片图像,返回结节候选框、可视化标注图与结构化报告。"""
    content = await file.read()
    return detect_nodules(content)
