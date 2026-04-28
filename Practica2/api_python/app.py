from flask import Flask, jsonify, request
import requests
import os
import psycopg2
from psycopg2.extras import RealDictCursor
import time

app = Flask(__name__)

# Configuracion de conexion emulando el diccionario de configuracion del profesor
DB_HOST = os.environ.get('PYSRV_DATABASE_HOST_POSTGRESQL', 'db-usuarios')
DB_NAME = os.environ.get('PYSRV_DATABASE_NAME', 'pokemon_db')
DB_USER = os.environ.get('PYSRV_DATABASE_USER', 'user_admin')
DB_PASS = os.environ.get('PYSRV_DATABASE_PASSWORD', '1234')
DB_PORT = os.environ.get('PYSRV_DATABASE_PORT', '5432')

def get_db_connection():
    # Reintento de conexión por si la DB tarda en iniciar
    retries = 5
    while retries > 0:
        try:
            conn = psycopg2.connect(
                host=DB_HOST,
                database=DB_NAME,
                user=DB_USER,
                password=DB_PASS
            )
            return conn
        except Exception as e:
            retries -= 1
            print(f"Esperando a la base de datos... ({retries} intentos restantes)")
            time.sleep(3)
    return None

# Inicialización de tabla en PostgreSQL
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
        # Insertar datos iniciales si la tabla está vacía
        cur.execute("SELECT COUNT(*) FROM entrenadores")
        if cur.fetchone()[0] == 0:
            cur.execute("INSERT INTO entrenadores (nombre, medallas) VALUES (%s, %s)", ('Ash Ketchum', 8))
            cur.execute("INSERT INTO entrenadores (nombre, medallas) VALUES (%s, %s)", ('Misty', 2))
        conn.commit()
        cur.close()
        conn.close()

init_db()

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

@app.route('/api/basedatos/entrenadores', methods=['POST'])
def crear_entrenador():
    conn = get_db_connection()
    if not conn:
        return jsonify({"error_tipo": "DB_ERROR", "mensaje": "Fallo de conexión"}), 500

    cursor = conn.cursor()
    try:
        data = request.json
        # SQL query to execute (estilo del profesor)
        query = "INSERT INTO entrenadores (nombre, medallas) VALUES (%s, %s)"
        cursor.execute(query, (data['nombre'], data['medallas']))

        # Confirmamos los cambios
        conn.commit()
        print("El registro ha sido insertado correctamente.")

        cursor.close()
        conn.close()
        return jsonify({"mensaje": "Registro completado"}), 201

    except (Exception, psycopg2.DatabaseError) as error:
        # Aquí aplicamos la lógica exacta de tu profesor
        print("Error: %s" % error)
        conn.rollback() # Deshace la transacción para evitar corrupción

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

@app.route('/api/archivo/<nombre_archivo>', methods=['GET'])
def procesar_archivo(nombre_archivo):
    # 1. Obtenemos la ruta absoluta de la carpeta donde se ejecuta app.py (/app)
    base_dir = os.path.dirname(os.path.abspath(__file__))

    # 2. Construimos la ruta blindada: /app/informes/nombre_archivo.txt
    ruta_archivo = os.path.join(base_dir, 'informes', f'{nombre_archivo}.txt')

    # Validación de existencia del recurso
    if not os.path.exists(ruta_archivo):
        return jsonify({
            "error_tipo": "NOT_FOUND",
            "mensaje": f"El documento no se encuentra. Python ha buscado exactamente en: {ruta_archivo}"
        }), 404

    try:
        with open(ruta_archivo, 'r', encoding='utf-8') as archivo:
            lineas = archivo.readlines()

        equipo = []
        for linea in lineas:
            registro = linea.strip().lower()
            if registro:
                # Consulta a la API externa para obtener los metadatos de cada registro
                r = requests.get(f'https://pokeapi.co/api/v2/pokemon/{registro}')
                if r.status_code == 200:
                    datos = r.json()
                    equipo.append({
                        "nombre": datos['name'].capitalize(),
                        "imagen": datos['sprites']['other']['official-artwork']['front_default']
                    })
                else:
                    # Si el registro no es válido, se anexa sin metadatos visuales
                    equipo.append({
                        "nombre": registro.capitalize(),
                        "imagen": None
                    })

        return jsonify(equipo), 200

    except Exception as e:
        return jsonify({
            "error_tipo": "INTERNAL_SERVER_ERROR",
            "mensaje": f"Error de procesamiento I/O: {str(e)}"
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)