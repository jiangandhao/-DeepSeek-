import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.model_selection import train_test_split
import os

class DataPreprocessor:
    def __init__(self):
        self.scaler = StandardScaler()
        self.label_encoders = {}

    def load_data(self, file_path):
        if file_path.endswith('.csv'):
            return pd.read_csv(file_path)
        elif file_path.endswith('.xlsx'):
            return pd.read_excel(file_path)
        else:
            raise ValueError(f"不支持的文件格式: {file_path}")

    def clean_data(self, df):
        df = df.dropna()
        df = df.drop_duplicates()
        return df

    def handle_missing_values(self, df, strategy='mean'):
        numeric_cols = df.select_dtypes(include=[np.number]).columns
        categorical_cols = df.select_dtypes(include=['object']).columns

        if strategy == 'mean':
            df[numeric_cols] = df[numeric_cols].fillna(df[numeric_cols].mean())
        elif strategy == 'median':
            df[numeric_cols] = df[numeric_cols].fillna(df[numeric_cols].median())
        elif strategy == 'mode':
            df[numeric_cols] = df[numeric_cols].fillna(df[numeric_cols].mode().iloc[0])

        df[categorical_cols] = df[categorical_cols].fillna(df[categorical_cols].mode().iloc[0])

        return df

    def encode_categorical(self, df, columns):
        for col in columns:
            if col not in self.label_encoders:
                self.label_encoders[col] = LabelEncoder()
                df[col] = self.label_encoders[col].fit_transform(df[col])
            else:
                df[col] = self.label_encoders[col].transform(df[col])
        return df

    def normalize_numeric(self, df, columns):
        df[columns] = self.scaler.fit_transform(df[columns])
        return df

    def split_data(self, X, y, test_size=0.2, val_size=0.1):
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=test_size, random_state=42
        )

        X_train, X_val, y_train, y_val = train_test_split(
            X_train, y_train, test_size=val_size/(1-test_size), random_state=42
        )

        return X_train, X_val, X_test, y_train, y_val, y_test

    def save_processed_data(self, data, file_path):
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        if isinstance(data, pd.DataFrame):
            data.to_csv(file_path, index=False)
        else:
            np.save(file_path, data)

def preprocess_blood_sugar_data(raw_data_path, output_path):
    preprocessor = DataPreprocessor()

    df = preprocessor.load_data(raw_data_path)

    df = preprocessor.clean_data(df)

    df = preprocessor.handle_missing_values(df)

    numeric_cols = ['blood_sugar_value', 'weight', 'height', 'age']
    categorical_cols = ['gender', 'meal_type']

    if all(col in df.columns for col in numeric_cols):
        df = preprocessor.normalize_numeric(df, numeric_cols)

    if all(col in df.columns for col in categorical_cols):
        df = preprocessor.encode_categorical(df, categorical_cols)

    preprocessor.save_processed_data(df, output_path)

    return df

def main():
    print("数据预处理模块已加载")
    print("使用示例:")
    print("  preprocessor = DataPreprocessor()")
    print("  df = preprocessor.load_data('data.csv')")
    print("  df = preprocessor.clean_data(df)")

if __name__ == "__main__":
    main()
