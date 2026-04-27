# Usamos una imagen ligera de Python
FROM python:3.10-slim

# Creamos una carpeta para la aplicación
WORKDIR /app

# Copiamos todos los archivos a esa carpeta
COPY . /app

# Instalamos las librerías necesarias
RUN pip install flask requests

# Exponemos el puerto
EXPOSE 5000

# Comando para ejecutar la API
CMD ["python", "app.py"]