import requests
import json
from typing import Dict, List, Optional

class DeepSeekClient:
    def __init__(self, api_key: str, api_url: str = "https://api.deepseek.com"):
        self.api_key = api_key
        self.api_url = api_url
        self.headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json"
        }

    def chat(self, messages: List[Dict[str, str]], model: str = "deepseek-chat") -> str:
        """
        发送聊天请求到 DeepSeek API
        """
        url = f"{self.api_url}/v1/chat/completions"
        payload = {
            "model": model,
            "messages": messages,
            "temperature": 0.7,
            "max_tokens": 2000
        }

        try:
            response = requests.post(url, headers=self.headers, json=payload, timeout=30)
            response.raise_for_status()
            result = response.json()
            return result["choices"][0]["message"]["content"]
        except requests.exceptions.RequestException as e:
            print(f"DeepSeek API 请求错误: {e}")
            return ""

    def get_health_recommendation(self, user_data: Dict) -> str:
        """
        获取健康建议
        """
        prompt = f"""
        基于以下健康数据，提供个性化的健康建议：
        用户信息：{json.dumps(user_data, ensure_ascii=False)}

        请提供：
        1. 健康风险评估
        2. 饮食建议
        3. 运动建议
        4. 生活方式建议
        """

        messages = [
            {"role": "system", "content": "你是一个专业的健康顾问，擅长根据用户的健康数据提供个性化的健康建议。"},
            {"role": "user", "content": prompt}
        ]

        return self.chat(messages)

    def analyze_blood_sugar(self, blood_sugar_data: List[Dict]) -> str:
        """
        分析血糖数据
        """
        prompt = f"""
        分析以下血糖数据并提供专业建议：
        血糖记录：{json.dumps(blood_sugar_data, ensure_ascii=False)}

        请提供：
        1. 血糖控制情况评估
        2. 血糖波动原因分析
        3. 饮食调整建议
        4. 运动建议
        """

        messages = [
            {"role": "system", "content": "你是一个专业的内分泌科医生，擅长分析血糖数据并提供糖尿病管理建议。"},
            {"role": "user", "content": prompt}
        ]

        return self.chat(messages)

    def assess_health_risk(self, health_profile: Dict) -> str:
        """
        评估健康风险
        """
        prompt = f"""
        基于以下健康档案，进行全面的健康风险评估：
        健康档案：{json.dumps(health_profile, ensure_ascii=False)}

        请评估：
        1. 患病风险（心血管疾病、糖尿病、高血压等）
        2. 风险等级（低/中/高）
        3. 预防建议
        4. 定期检查建议
        """

        messages = [
            {"role": "system", "content": "你是一个专业的健康管理师，擅长根据用户的健康档案评估患病风险并提供预防建议。"},
            {"role": "user", "content": prompt}
        ]

        return self.chat(messages)
