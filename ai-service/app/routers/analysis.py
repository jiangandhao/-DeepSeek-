"""血糖分析路由:趋势预测 + 异常检测。"""
from fastapi import APIRouter

from app.anomaly.detector import detect
from app.prediction.predictor import predict
from app.schemas import AnomalyRequest, AnomalyResponse, PredictRequest, PredictResponse

router = APIRouter(prefix="/api/analysis", tags=["analysis"])


@router.post("/predict", response_model=PredictResponse)
async def predict_glucose(req: PredictRequest):
    return predict(req.history, req.horizon)


@router.post("/anomaly", response_model=AnomalyResponse)
async def detect_anomaly(req: AnomalyRequest):
    return AnomalyResponse(anomalies=detect(req.history))
