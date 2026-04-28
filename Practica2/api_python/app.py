from flask import Flask, jsonify, request
import requests
import os
import psycopg2
from psycopg2.extras import RealDictCursor
import time

app = Flask(__name__)

# Configuración de conexión dinámica
DB_HOST = os.environ.get('PYSRV_DATABASE_HOST_POSTGRESQL', 'db-usuarios')
DB_NAME = os.environ.get('PYSRV_DATABASE_NAME', 'pokemon_db')
DB_USER = os.environ.get('PYSRV_DATABASE_USER', 'user_admin')
DB_PASS = os.environ.get('PYSRV_DATABASE_PASSWORD', '1234')
DB_PORT = os.environ.get('PYSRV_DATABASE_PORT', '5432')

def get_db_connection():
    retries = 5
    while retries > 0:
        try:
            conn = psycopg2.connect(
                host=DB_HOST, database=DB_NAME, user=DB_USER, password=DB_PASS, port=DB_PORT
            )
            return conn
        except Exception as e:
            retries -= 1
            print(f"Esperando a la base de datos... ({retries} intentos restantes)")
            time.sleep(3)
    return None

def init_db():
    conn = get_db_connection()
    if conn:
        cur = conn.cursor()
        cur.execute('''
            CREATE TABLE IF NOT EXISTS entrenadores (
                id SERIAL PRIMARY KEY,
                nombre VARCHAR(100) NOT NULL,
                medallas INTEGER NOT NULL
            )
        ''')
        cur.execute("SELECT COUNT(*) FROM entrenadores")
        if cur.fetchone()[0] == 0:
            cur.execute("INSERT INTO entrenadores (nombre, medallas) VALUES (%s, %s)", ('Ash Ketchum', 8))
            cur.execute("INSERT INTO entrenadores (nombre, medallas) VALUES (%s, %s)", ('Misty', 2))
        conn.commit()
        cur.close()
        conn.close()

init_db()

# --- 1. BUSCADOR POKEAPI ---
@app.route('/api/pokemon/<nombre>', methods=['GET'])
def buscar_pokemon(nombre):
    try:
        r = requests.get(f'https://pokeapi.co/api/v2/pokemon/{nombre.lower()}')
        r.raise_for_status()
        d = r.json()
        return jsonify({
            "nombre": d['name'].capitalize(),
            "imagen": d['sprites']['other']['official-artwork']['front_default'],
            "experiencia": d['base_experience'],
            "altura": d['height']/10,
            "peso": d['weight']/10
        }), 200
    except Exception as e:
        return jsonify({"error_tipo": "API_THIRD_PARTY_ERROR", "mensaje": str(e)}), 502

# --- 2. BASE DE DATOS (POST Y GET) ---
@app.route('/api/basedatos/entrenadores', methods=['POST'])
def crear_entrenador():
    conn = get_db_connection()
    if not conn:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": "Fallo de conexión"}), 500

    cursor = conn.cursor()
    try:
        data = request.json
        query = "INSERT INTO entrenadores (nombre, medallas) VALUES (%s, %s)"
        cursor.execute(query, (data['nombre'], data['medallas']))
        conn.commit()
        cursor.close()
        conn.close()
        return jsonify({"mensaje": "Registro completado"}), 201
    except (Exception, psycopg2.DatabaseError) as error:
        conn.rollback()
        cursor.close()
        conn.close()
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": str(error)}), 500

@app.route('/api/basedatos/<tabla>', methods=['GET'])
def leer_basedatos(tabla):
    if tabla != "entrenadores":
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": "Tabla no autorizada"}), 500
    conn = get_db_connection()
    try:
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute("SELECT * FROM entrenadores")
        filas = cur.fetchall()
        cur.close()
        conn.close()
        return jsonify({"datos": filas}), 200
    except Exception as e:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": str(e)}), 500

# --- 3. LECTURA DE ARCHIVOS (RUTA ABSOLUTA ARREGLADA) ---
@app.route('/api/archivo/<nombre_archivo>', methods=['GET'])
def procesar_archivo(nombre_archivo):
    base_dir = os.path.dirname(os.path.abspath(__file__))
    ruta_archivo = os.path.join(base_dir, 'informes', f'{nombre_archivo}.txt')

    if not os.path.exists(ruta_archivo):
        return jsonify({
            "error_tipo": "NOT_FOUND",
            "mensaje": f"El documento no se encuentra en la ruta: {ruta_archivo}"
        }), 404

    try:
        with open(ruta_archivo, 'r', encoding='utf-8') as archivo:
            lineas = archivo.readlines()

        equipo = []
        for linea in lineas:
                    registro = linea.strip().lower()
                    if registro:
                        r = requests.get(f'https://pokeapi.co/api/v2/pokemon/{registro}')
                        if r.status_code == 200:
                            datos = r.json()
                            equipo.append({
                                "nombre": datos['name'].capitalize(),
                                "imagen": datos['sprites']['other']['official-artwork']['front_default'],
                                "experiencia": datos['base_experience'],
                                "altura": datos['height']/10,
                                "peso": datos['weight']/10
                            })
                        else:
                            equipo.append({
                                "nombre": registro.capitalize(),
                                "imagen": None,
                                "experiencia": 0, "altura": 0, "peso": 0
                            })
        return jsonify(equipo), 200
    except Exception as e:
        return jsonify({"error_tipo": "INTERNAL_SERVER_ERROR", "mensaje": str(e)}), 500

# --- 4. SIMULADOR DE EXCEPCIONES (EL QUE TE FALTABA) ---
@app.route('/api/test-error/<codigo>', methods=['GET'])
def forzar_error(codigo):
    if codigo == '400':
        return jsonify({"error_tipo": "BAD_REQUEST", "mensaje": "Faltan parámetros en la petición."}), 400
    elif codigo == '401':
        return jsonify({"error_tipo": "UNAUTHORIZED", "mensaje": "Token de seguridad inválido o caducado."}), 401
    elif codigo == '404':
        return jsonify({"error_tipo": "NOT_FOUND", "mensaje": "El entrenador o Pokémon solicitado no existe."}), 404
    elif codigo == '500':
        return jsonify({"error_tipo": "INTERNAL_SERVER_ERROR", "mensaje": "Fallo catastrófico en el disco duro."}), 500
    else:
        return jsonify({"mensaje": "Todo funciona correctamente (Status 200)."}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)