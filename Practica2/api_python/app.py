# app.py
from flask import Flask, jsonify
import sqlite3
import requests

app = Flask(__name__)

# 1. Simular Excepción de apertura y lectura de archivos
@app.route('/api/error/archivo', methods=['GET'])
def error_archivo():
    try:
        # Intentamos abrir un archivo que NO existe
        with open('un_archivo_que_no_existe.txt', 'r') as file:
            contenido = file.read()
        return jsonify({"mensaje": "Archivo leído con éxito"}), 200
    except FileNotFoundError as e:
        # Capturamos el error específico y devolvemos un código HTTP 404 (Not Found)
        return jsonify({
            "error_tipo": "FILE_ERROR",
            "mensaje_original": str(e)
        }), 404

# 2. Simular Excepción de accesos a bases de datos
@app.route('/api/error/basedatos', methods=['GET'])
def error_basedatos():
    try:
        # Nos conectamos a una base de datos temporal en memoria
        conexion = sqlite3.connect(':memory:')
        cursor = conexion.cursor()
        # Intentamos hacer una consulta a una tabla que NUNCA hemos creado
        cursor.execute('SELECT * FROM tabla_fantasma')
        return jsonify({"mensaje": "Consulta exitosa"}), 200
    except sqlite3.OperationalError as e:
        # Capturamos el error de base de datos y devolvemos un código HTTP 500 (Internal Server Error)
        return jsonify({
            "error_tipo": "DB_ERROR",
            "mensaje_original": str(e)
        }), 500

# 3. Simular Excepción de llamadas a APIs de terceros (Pokémon)
@app.route('/api/error/pokemon', methods=['GET'])
def error_pokemon():
    try:
        # Llamamos a la API real de Pokémon pidiendo uno que no existe
        respuesta = requests.get('https://pokeapi.co/api/v2/pokemon/digimon_no_es_pokemon')
        # Esto hace que lance un error si la respuesta no es exitosa (como un 404)
        respuesta.raise_for_status()
        return jsonify(respuesta.json()), 200
    except requests.exceptions.HTTPError as e:
        # Capturamos el error de la API externa y devolvemos un código HTTP 502 (Bad Gateway)
        return jsonify({
            "error_tipo": "API_THIRD_PARTY_ERROR",
            "mensaje_original": str(e)
        }), 502

if __name__ == '__main__':
    # Iniciamos el servidor en el puerto 5000
    print("Iniciando la API de prueba en http://localhost:5000")
    app.run(debug=True, port=5000)
