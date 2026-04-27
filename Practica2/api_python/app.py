from flask import Flask, jsonify
import sqlite3
import requests

app = Flask(__name__)

# --- AUTO-CONFIGURACIÓN INICIAL ---
with open('notas.txt', 'w', encoding='utf-8') as f:
    f.write("¡Hola! Este es un mensaje secreto sobre tácticas Pokémon leído desde un archivo.")

# NUEVO: Base de datos SQLite de Entrenadores Pokémon
conexion = sqlite3.connect('pokemon_local.db')
cursor = conexion.cursor()
cursor.execute('CREATE TABLE IF NOT EXISTS entrenadores (id INTEGER PRIMARY KEY, nombre TEXT, medallas INTEGER)')
cursor.execute('DELETE FROM entrenadores')
cursor.execute("INSERT INTO entrenadores (nombre, medallas) VALUES ('Ash Ketchum', 8)")
cursor.execute("INSERT INTO entrenadores (nombre, medallas) VALUES ('Misty', 2)")
cursor.execute("INSERT INTO entrenadores (nombre, medallas) VALUES ('Brock', 1)")
conexion.commit()
conexion.close()
# ----------------------------------

@app.route('/api/archivo/<nombre>', methods=['GET'])
def leer_archivo(nombre):
    try:
        with open(f'{nombre}.txt', 'r', encoding='utf-8') as file:
            return jsonify({"mensaje": "Éxito", "contenido": file.read()}), 200
    except FileNotFoundError as e:
        return jsonify({"error_tipo": "FILE_ERROR", "mensaje_original": str(e)}), 404

# Lee de la BD pokemon_local.db
@app.route('/api/basedatos/<tabla>', methods=['GET'])
def leer_basedatos(tabla):
    try:
        conexion = sqlite3.connect('pokemon_local.db')
        cursor = conexion.cursor()
        cursor.execute(f'SELECT * FROM {tabla}')
        filas = cursor.fetchall()
        conexion.close()
        return jsonify({"mensaje": "Consulta exitosa", "datos": filas}), 200
    except sqlite3.OperationalError as e:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje_original": str(e)}), 500

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
    app.run(host='0.0.0.0', debug=True, port=5000)