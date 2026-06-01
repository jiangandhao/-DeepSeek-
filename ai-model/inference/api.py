from flask import Flask, request, jsonify
from flask_cors import CORS
import torch
import numpy as np
import os
import sys

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from models.blood_sugar_model import BloodSugarModel
from models.risk_assessment_model import RiskAssessmentModel
from deepseek.deepseek_client import DeepSeekClient

app = Flask(__name__)
CORS(app)

models = {}
deepseek_client = None

def load_models():
    global models, deepseek_client

    blood_sugar_model = BloodSugarModel(input_dim=10, hidden_dim=64, output_dim=1)
    blood_sugar_path = "../../data/models/blood_sugar_model.pth"
    if os.path.exists(blood_sugar_path):
        checkpoint = torch.load(blood_sugar_path)
        blood_sugar_model.load_state_dict(checkpoint['model_state_dict'])
    models['blood_sugar'] = blood_sugar_model

    risk_model = RiskAssessmentModel(input_dim=15, hidden_dims=[128, 64, 32], output_dim=3)
    models['risk_assessment'] = risk_model

    api_key = os.environ.get('DEEPSEEK_API_KEY', 'your-api-key-here')
    deepseek_client = DeepSeekClient(api_key)

@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'healthy'})

@app.route('/api/blood-sugar/predict', methods=['POST'])
def predict_blood_sugar():
    try:
        data = request.json
        features = torch.FloatTensor(data['features']).unsqueeze(0)

        models['blood_sugar'].eval()
        with torch.no_grad():
            prediction = models['blood_sugar'](features)

        return jsonify({
            'predicted_value': prediction.item(),
            'status': 'success'
        })
    except Exception as e:
        return jsonify({'error': str(e), 'status': 'error'}), 400

@app.route('/api/risk-assessment/predict', methods=['POST'])
def predict_risk():
    try:
        data = request.json
        features = torch.FloatTensor(data['features']).unsqueeze(0)

        models['risk_assessment'].eval()
        with torch.no_grad():
            probs = models['risk_assessment'](features)
            probs = torch.softmax(probs, dim=1)

        risk_levels = ['低风险', '中风险', '高风险']
        risk_probs = probs.numpy()[0]

        return jsonify({
            'risk_level': risk_levels[np.argmax(risk_probs)],
            'risk_probabilities': {
                'low': float(risk_probs[0]),
                'medium': float(risk_probs[1]),
                'high': float(risk_probs[2])
            },
            'status': 'success'
        })
    except Exception as e:
        return jsonify({'error': str(e), 'status': 'error'}), 400

@app.route('/api/deepseek/recommendation', methods=['POST'])
def get_recommendation():
    try:
        data = request.json
        user_data = data.get('user_data', {})

        recommendation = deepseek_client.get_health_recommendation(user_data)

        return jsonify({
            'recommendation': recommendation,
            'status': 'success'
        })
    except Exception as e:
        return jsonify({'error': str(e), 'status': 'error'}), 400

@app.route('/api/deepseek/analyze-blood-sugar', methods=['POST'])
def analyze_blood_sugar():
    try:
        data = request.json
        blood_sugar_data = data.get('blood_sugar_data', [])

        analysis = deepseek_client.analyze_blood_sugar(blood_sugar_data)

        return jsonify({
            'analysis': analysis,
            'status': 'success'
        })
    except Exception as e:
        return jsonify({'error': str(e), 'status': 'error'}), 400

@app.route('/api/deepseek/assess-risk', methods=['POST'])
def assess_risk():
    try:
        data = request.json
        health_profile = data.get('health_profile', {})

        assessment = deepseek_client.assess_health_risk(health_profile)

        return jsonify({
            'assessment': assessment,
            'status': 'success'
        })
    except Exception as e:
        return jsonify({'error': str(e), 'status': 'error'}), 400

if __name__ == '__main__':
    load_models()
    app.run(host='0.0.0.0', port=5000, debug=True)
