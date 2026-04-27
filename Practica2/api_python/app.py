from flask import Flask, jsonify
import sqlite3
import requests
import os

app = Flask(__name__)

# --- AUTO-CONFIGURACIÓN INICIAL ---
# 1. Crear archivo de texto de prueba automáticamente para que no te falle
with open('notas.txt', 'w', encoding='utf-8') as f:
    f.write("¡Hola! Este es un mensaje secreto leído desde un archivo de texto real.")

# 2. Crear base de datos SQLite y meter usuarios reales
conexion = sqlite3.connect('mi_base_de_datos.db')
cursor = conexion.cursor()
cursor.execute('CREATE TABLE IF NOT EXISTS usuarios (id INTEGER PRIMARY KEY, nombre TEXT, rol TEXT)')
cursor.execute('DELETE FROM usuarios') # Limpiamos por si reinicias
cursor.execute("INSERT INTO usuarios (nombre, rol) VALUES ('Ana', 'Administradora')")
cursor.execute("INSERT INTO usuarios (nombre, rol) VALUES ('Luis', 'Usuario Normal')")
conexion.commit()
conexion.close()
# ----------------------------------

# Archivos
@app.route('/api/archivo/<nombre>', methods=['GET'])
def leer_archivo(nombre):
    try:
        with open(f'{nombre}.txt', 'r', encoding='utf-8') as file:
            contenido = file.read()
        return jsonify({"mensaje": "Éxito", "contenido": contenido}), 200
    except FileNotFoundError as e:
        return jsonify({"error_tipo": "FILE_ERROR", "mensaje_original": str(e)}), 404

# Base de datos (recibe el nombre de la tabla)
@app.route('/api/basedatos/<tabla>', methods=['GET'])
def leer_basedatos(tabla):
    try:
        conexion = sqlite3.connect('mi_base_de_datos.db')
        cursor = conexion.cursor()
        # Intentamos leer la tabla que pida el usuario
        cursor.execute(f'SELECT * FROM {tabla}')
        filas = cursor.fetchall()
        conexion.close()
        return jsonify({"mensaje": "Consulta exitosa", "datos": filas}), 200
    except sqlite3.OperationalError as e:
        # Si pide una tabla que no existe, salta la excepción
        return jsonify({"error_tipo": "DB_ERROR", "mensaje_original": str(e)}), 500

# PokeAPI
@app.route('/api/pokemon/<nombre>', methods=['GET'])
def buscar_pokemon(nombre):
    try:
        respuesta = requests.get(f'https://pokeapi.co/api/v2/pokemon/{nombre}')
        respuesta.raise_for_status()
        datos = respuesta.json()
        return jsonify({"nombre": datos['name'], "experiencia_base": datos['base_experience']}), 200
    except requests.exceptions.HTTPError as e:
        return jsonify({"error_tipo": "API_THIRD_PARTY_ERROR", "mensaje_original": str(e)}), 502

if __name__ == '__main__':
    app.run(debug=True, port=5000)
