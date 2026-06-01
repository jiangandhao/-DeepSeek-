import torch
import torch.nn as nn

class BloodSugarModel(nn.Module):
    """
    血糖预测模型
    基于 LSTM + Attention 机制
    """
    def __init__(self, input_dim=10, hidden_dim=64, num_layers=2, output_dim=1):
        super(BloodSugarModel, self).__init__()
        self.hidden_dim = hidden_dim
        self.num_layers = num_layers

        self.lstm = nn.LSTM(
            input_size=input_dim,
            hidden_size=hidden_dim,
            num_layers=num_layers,
            batch_first=True,
            dropout=0.2
        )

        self.attention = nn.MultiheadAttention(
            embed_dim=hidden_dim,
            num_heads=4,
            dropout=0.1
        )

        self.fc = nn.Sequential(
            nn.Linear(hidden_dim, 32),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(32, output_dim)
        )

    def forward(self, x):
        # LSTM
        lstm_out, _ = self.lstm(x)

        # Attention
        attn_out, _ = self.attention(lstm_out, lstm_out, lstm_out)

        # 全连接层
        out = self.fc(attn_out[:, -1, :])

        return out

    def predict(self, x):
        self.eval()
        with torch.no_grad():
            return self.forward(x)

class BloodSugarLSTM(nn.Module):
    """
    简化版血糖预测模型
    """
    def __init__(self, input_dim=5, hidden_dim=32, output_dim=1):
        super(BloodSugarLSTM, self).__init__()
        self.lstm = nn.LSTM(input_dim, hidden_dim, batch_first=True)
        self.fc = nn.Linear(hidden_dim, output_dim)

    def forward(self, x):
        out, _ = self.lstm(x)
        out = self.fc(out[:, -1, :])
        return out
