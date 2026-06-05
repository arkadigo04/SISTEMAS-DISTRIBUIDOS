from flask import Flask, jsonify, request
import requests
import os
import psycopg2
from psycopg2.extras import RealDictCursor
import time
import hashlib
import json
from time import time

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
        cur.execute('''
                    CREATE TABLE IF NOT EXISTS gimnasios (
                        id SERIAL PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL,
                        ciudad VARCHAR(100) NOT NULL
                    )
                ''')
        cur.execute("SELECT COUNT(*) FROM entrenadores")
        if cur.fetchone()[0] == 0:
            cur.execute("INSERT INTO entrenadores (nombre, medallas) VALUES (%s, %s)", ('Ash Ketchum', 8))
            cur.execute("INSERT INTO entrenadores (nombre, medallas) VALUES (%s, %s)", ('Misty', 2))
        cur.execute("SELECT COUNT(*) FROM gimnasios")
        if cur.fetchone()[0] == 0:
            cur.execute("INSERT INTO gimnasios (nombre, ciudad) VALUES (%s, %s)", ('Gimnasio Roca', 'Ciudad Plateada'))
        conn.commit()
        cur.close()
        conn.close()


init_db()

# --- SISTEMA BLOCKCHAIN (Registro Inmutable de Transferencias) ---
class Blockchain:
    def __init__(self):
        self.chain = []
        # Crear el bloque Génesis (el primer bloque de la cadena)
        self.crear_bloque(proof=100, previous_hash='1', transaccion="Bloque Genesis - Inicio de la Red")

    def crear_bloque(self, proof, previous_hash, transaccion):
        bloque = {
            'index': len(self.chain) + 1,
            'timestamp': time(),
            'transaccion': transaccion,
            'proof': proof,
            'previous_hash': previous_hash
        }
        self.chain.append(bloque)
        return bloque

    def obtener_bloque_anterior(self):
        return self.chain[-1]

    def prueba_de_trabajo(self, previous_proof):
        # Un sistema de Proof of Work hiper simplificado
        new_proof = 1
        check_proof = False
        while check_proof is False:
            hash_operation = hashlib.sha256(str(new_proof**2 - previous_proof**2).encode()).hexdigest()
            if hash_operation[:4] == '0000':
                check_proof = True
            else:
                new_proof += 1
        return new_proof

    def hash(self, bloque):
        encoded_block = json.dumps(bloque, sort_keys=True).encode()
        return hashlib.sha256(encoded_block).hexdigest()

# Instanciamos la Blockchain a nivel global
liga_blockchain = Blockchain()

# Endpoint 1: Ver toda la cadena de bloques
@app.route('/api/blockchain/cadena', methods=['GET'])
def obtener_cadena():
    response = {
        'cadena': liga_blockchain.chain,
        'longitud': len(liga_blockchain.chain)
    }
    return jsonify(response), 200

# Endpoint 2: Minar un nuevo bloque (Registrar una transacción/intercambio)
@app.route('/api/blockchain/minar', methods=['POST'])
def minar_bloque():
    data = request.json
    if not data or 'transaccion' not in data:
        return jsonify({'error_tipo': 'BAD_REQUEST', 'mensaje': 'Falta el dato de transaccion'}), 400

    bloque_anterior = liga_blockchain.obtener_bloque_anterior()
    proof_anterior = bloque_anterior['proof']

    # 1. Ejecutar el algoritmo de minado (Proof of Work)
    proof = liga_blockchain.prueba_de_trabajo(proof_anterior)

    # 2. Generar el hash del bloque anterior para enlazarlo
    hash_anterior = liga_blockchain.hash(bloque_anterior)

    # 3. Crear el nuevo bloque
    bloque = liga_blockchain.crear_bloque(proof, hash_anterior, data['transaccion'])

    return jsonify({
        'mensaje': '¡Bloque minado y añadido a la red Blockchain!',
        'index': bloque['index'],
        'transaccion': bloque['transaccion'],
        'hash_anterior': bloque['previous_hash']
    }), 201

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
    if tabla not in ["entrenadores", "gimnasios"]:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": "Tabla no autorizada"}), 500

    conn = get_db_connection()
    try:
        cur = conn.cursor(cursor_factory=RealDictCursor)
        cur.execute(f"SELECT * FROM {tabla}")
        filas = cur.fetchall()
        cur.close()
        conn.close()
        return jsonify({"datos": filas}), 200
    except Exception as e:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": str(e)}), 500

@app.route('/api/basedatos/entrenadores/<int:id>', methods=['PUT'])
def actualizar_entrenador(id):
    conn = get_db_connection()
    if not conn:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": "Fallo de conexión"}), 500
    cursor = conn.cursor()
    try:
        data = request.json
        query = "UPDATE entrenadores SET nombre = %s, medallas = %s WHERE id = %s"
        cursor.execute(query, (data['nombre'], data['medallas'], id))
        conn.commit()
        return jsonify({"mensaje": "Registro actualizado"}), 200
    except Exception as error:
        conn.rollback()
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": str(error)}), 500
    finally:
        cursor.close()
        conn.close()

@app.route('/api/basedatos/entrenadores/<int:id>', methods=['DELETE'])
def borrar_entrenador(id):
    conn = get_db_connection()
    if not conn:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": "Fallo de conexión"}), 500
    cursor = conn.cursor()
    try:
        cursor.execute("DELETE FROM entrenadores WHERE id = %s", (id,))
        conn.commit()
        return jsonify({"mensaje": "Registro eliminado"}), 200
    except Exception as error:
        conn.rollback()
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": str(error)}), 500
    finally:
        cursor.close()
        conn.close()

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

@app.route('/api/basedatos/gimnasios', methods=['POST'])
def crear_gimnasio():
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        data = request.json
        cursor.execute("INSERT INTO gimnasios (nombre, ciudad) VALUES (%s, %s)", (data['nombre'], data['ciudad']))
        conn.commit()
        return jsonify({"mensaje": "Gimnasio registrado"}), 201
    except Exception as e:
        conn.rollback()
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": str(e)}), 500
    finally:
        cursor.close()
        conn.close()

@app.route('/api/basedatos/gimnasios/<int:id>', methods=['PUT'])
def actualizar_gimnasio(id):
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        data = request.json
        cursor.execute("UPDATE gimnasios SET nombre = %s, ciudad = %s WHERE id = %s", (data['nombre'], data['ciudad'], id))
        conn.commit()
        return jsonify({"mensaje": "Gimnasio actualizado"}), 200
    except Exception as e:
        conn.rollback()
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": str(e)}), 500
    finally:
        cursor.close()
        conn.close()

@app.route('/api/basedatos/gimnasios/<int:id>', methods=['DELETE'])
def borrar_gimnasio(id):
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("DELETE FROM gimnasios WHERE id = %s", (id,))
        conn.commit()
        return jsonify({"mensaje": "Gimnasio eliminado"}), 200
    except Exception as e:
        conn.rollback()
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": str(e)}), 500
    finally:
        cursor.close()
        conn.close()

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