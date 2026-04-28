from flask import Flask, jsonify, request
import sqlite3
import requests
import os

app = Flask(__name__)

# --- AUTO-CONFIGURACIÓN INICIAL ---
if not os.path.exists('informes'):
    os.makedirs('informes')

# ¡Creamos el archivo txt con saltos de línea (enters) como pediste!
with open('informes/equipo.txt', 'w', encoding='utf-8') as f:
    f.write("pikachu\ncharizard\nsnorlax\nmewtwo")

# Base de datos SQLite
conexion = sqlite3.connect('pokemon_local.db')
cursor = conexion.cursor()
cursor.execute('CREATE TABLE IF NOT EXISTS entrenadores (id INTEGER PRIMARY KEY, nombre TEXT, medallas INTEGER)')
conexion.commit()
conexion.close()
# ----------------------------------

# 1. LA NUEVA LÓGICA DE ARCHIVOS (Lee el txt y busca en PokeAPI)
@app.route('/api/archivo/<nombre>', methods=['GET'])
def leer_archivo(nombre):
    try:
        ruta = os.path.join('informes', f'{nombre}.txt')
        with open(ruta, 'r', encoding='utf-8') as file:
            nombres_pokemon = file.read().splitlines() # Separa por los "enters"

        equipo_final = []
        for nombre_poke in nombres_pokemon:
            nombre_limpio = nombre_poke.strip().lower()
            if not nombre_limpio:
                continue

            try:
                # Busca cada pokemon del TXT en la API
                r = requests.get(f'https://pokeapi.co/api/v2/pokemon/{nombre_limpio}')
                r.raise_for_status()
                d = r.json()
                equipo_final.append({
                    "nombre": d['name'].capitalize(),
                    "imagen": d['sprites']['other']['official-artwork']['front_default']
                })
            except:
                # Si en el txt hay un nombre inventado, ponemos una Pokeball
                equipo_final.append({
                    "nombre": f"{nombre_poke} (Desconocido)",
                    "imagen": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/poke-ball.png"
                })

        return jsonify({"mensaje": "Archivo leído", "equipo": equipo_final}), 200

    except FileNotFoundError:
        return jsonify({"error_tipo": "FILE_ERROR", "mensaje_original": "Archivo no encontrado"}), 404


# 2. ENTRENADORES POST (Guardar)
@app.route('/api/basedatos/entrenadores', methods=['POST'])
def crear_entrenador():
    try:
        data = request.json
        conn = sqlite3.connect('pokemon_local.db')
        cursor = conn.cursor()
        cursor.execute("INSERT INTO entrenadores (nombre, medallas) VALUES (?, ?)", (data['nombre'], data['medallas']))
        conn.commit()
        conn.close()
        return jsonify({"mensaje": "OK"}), 201
    except Exception as e:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje_original": str(e)}), 500

# 3. ENTRENADORES GET (Consultar)
@app.route('/api/basedatos/<tabla>', methods=['GET'])
def leer_basedatos(tabla):
    try:
        conn = sqlite3.connect('pokemon_local.db')
        cursor = conn.cursor()
        cursor.execute(f'SELECT * FROM {tabla}')
        filas = [{"id": f[0], "nombre": f[1], "medallas": f[2]} for f in cursor.fetchall()]
        conn.close()
        return jsonify({"datos": filas}), 200
    except:
        return jsonify({"error_tipo": "DB_ERROR"}), 500

# 4. BUSCADOR POKEAPI NORMAL
@app.route('/api/pokemon/<nombre>', methods=['GET'])
def buscar_pokemon(nombre):
    try:
        r = requests.get(f'https://pokeapi.co/api/v2/pokemon/{nombre}')
        r.raise_for_status()
        d = r.json()
        return jsonify({
            "nombre": d['name'].capitalize(),
            "imagen": d['sprites']['other']['official-artwork']['front_default'],
            # Corregimos las claves para que no te dé el error SpEL de antes
            "experiencia": d['base_experience'],
            "altura": d['height']/10,
            "peso": d['weight']/10
        }), 200
    except:
        return jsonify({"error_tipo": "API_THIRD_PARTY_ERROR"}), 502

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True, port=5000)