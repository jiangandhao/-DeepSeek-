import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset
import numpy as np
import os
import sys

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from models.blood_sugar_model import BloodSugarModel

class BloodSugarTrainer:
    def __init__(self, model, learning_rate=0.001):
        self.model = model
        self.criterion = nn.MSELoss()
        self.optimizer = optim.Adam(model.parameters(), lr=learning_rate)
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        self.model.to(self.device)

    def train(self, train_loader, epochs=100):
        self.model.train()
        train_losses = []

        for epoch in range(epochs):
            total_loss = 0
            for batch_x, batch_y in train_loader:
                batch_x = batch_x.to(self.device)
                batch_y = batch_y.to(self.device)

                self.optimizer.zero_grad()
                outputs = self.model(batch_x)
                loss = self.criterion(outputs.squeeze(), batch_y)
                loss.backward()
                self.optimizer.step()

                total_loss += loss.item()

            avg_loss = total_loss / len(train_loader)
            train_losses.append(avg_loss)

            if (epoch + 1) % 10 == 0:
                print(f'Epoch [{epoch+1}/{epochs}], Loss: {avg_loss:.4f}')

        return train_losses

    def evaluate(self, test_loader):
        self.model.eval()
        total_loss = 0
        predictions = []
        actuals = []

        with torch.no_grad():
            for batch_x, batch_y in test_loader:
                batch_x = batch_x.to(self.device)
                batch_y = batch_y.to(self.device)

                outputs = self.model(batch_x)
                loss = self.criterion(outputs.squeeze(), batch_y)
                total_loss += loss.item()

                predictions.extend(outputs.squeeze().cpu().numpy())
                actuals.extend(batch_y.cpu().numpy())

        avg_loss = total_loss / len(test_loader)
        rmse = np.sqrt(np.mean((np.array(predictions) - np.array(actuals))**2))

        return avg_loss, rmse

    def save_model(self, path):
        torch.save({
            'model_state_dict': self.model.state_dict(),
            'optimizer_state_dict': self.optimizer.state_dict(),
        }, path)
        print(f'模型已保存到: {path}')

    def load_model(self, path):
        checkpoint = torch.load(path)
        self.model.load_state_dict(checkpoint['model_state_dict'])
        self.optimizer.load_state_dict(checkpoint['optimizer_state_dict'])
        print(f'模型已加载: {path}')

def generate_sample_data(num_samples=1000):
    """
    生成示例数据用于测试
    """
    np.random.seed(42)
    X = np.random.randn(num_samples, 5, 10).astype(np.float32)
    y = np.random.randn(num_samples).astype(np.float32)

    X_tensor = torch.FloatTensor(X)
    y_tensor = torch.FloatTensor(y)

    return X_tensor, y_tensor

def main():
    input_dim = 10
    hidden_dim = 64
    output_dim = 1
    epochs = 100
    batch_size = 32
    learning_rate = 0.001

    model = BloodSugarModel(input_dim=input_dim, hidden_dim=hidden_dim, output_dim=output_dim)

    X, y = generate_sample_data(1000)
    dataset = TensorDataset(X, y)
    train_size = int(0.8 * len(dataset))
    test_size = len(dataset) - train_size
    train_dataset, test_dataset = torch.utils.data.random_split(dataset, [train_size, test_size])

    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    test_loader = DataLoader(test_dataset, batch_size=batch_size, shuffle=False)

    trainer = BloodSugarTrainer(model, learning_rate=learning_rate)

    print("开始训练...")
    train_losses = trainer.train(train_loader, epochs=epochs)

    print("\n开始评估...")
    test_loss, rmse = trainer.evaluate(test_loader)
    print(f'测试损失: {test_loss:.4f}')
    print(f'RMSE: {rmse:.4f}')

    model_path = "../../data/models/blood_sugar_model.pth"
    os.makedirs(os.path.dirname(model_path), exist_ok=True)
    trainer.save_model(model_path)

if __name__ == "__main__":
    main()
