Duoc UC - Escuela de Informática y Telecomunicaciones

# 🧠 Evaluación Final Transversal – Desarrollo Orientado a Objetos II

## 👤 Autor del proyecto

- **Nombre completo:** Pablo Roberto Smith Ortiz
- **Carrera:** Analista Programador
- **Sede:** Online

---

## 📘 Descripción general del sistema

**Sistema de Gestión de Biblioteca Escolar** en Java + Swing, con persistencia en MySQL vía JDBC.

- **Login por rol** (RUT o correo): el bibliotecario gestiona todo; el estudiante consulta el catálogo y su historial.
- **CRUD** de libros, estudiantes y categorías.
- **Préstamos y devoluciones** con control de stock, vencimiento automático (7 días) y registro del atraso.
- **Reportes:** libros más prestados, libros en préstamo e historial por estudiante.

---

## 🧩 Estructura general del proyecto

```
biblioteca-escolar-java-mysql/
├── lib/                 # Conector JDBC de MySQL
├── sql/                 # Scripts de creación y poblado
├── src/
│   ├── main/            # Punto de entrada
│   ├── modelo/          # Entidades, jerarquía de usuarios y records de lectura
│   ├── dao/             # Interfaces DAO y excepciones de persistencia
│   │   └── impl/        # Implementaciones JDBC
│   ├── controlador/     # Reglas de negocio
│   ├── vista/           # Pantallas y diálogos Swing
│   └── utils/           # DatabaseConnection (Singleton), validación y hash
└── db.properties.example
```

---

## 🧵 Patrones, hilos y sincronización

- **MVC + DAO:** `vista → controlador → dao → MySQL`; los controladores dependen de interfaces DAO.
- **Singleton:** `DatabaseConnection.getInstance()` mantiene una única conexión, reabierta si se corta.
- **Sincronización:** todo acceso a la BD se sincroniza sobre esa instancia, y el préstamo descuenta stock con `UPDATE ... WHERE stock > 0` en una transacción.
- **Hilos:** el préstamo se registra con `SwingWorker`. **Préstamos → Simular préstamos simultáneos** lanza N hilos sobre un mismo libro y verifica que el stock nunca baje de más.

---

## 🧭 Decisiones de diseño

- **Roles polimórficos:** `Usuario` abstracta → `Bibliotecario` / `UsuarioEstudiante`, cada uno con sus permisos; el menú se arma con ellos.
- **Excepciones:** `RestriccionException` hereda de `PersistenciaException`; las reglas de datos se muestran como advertencia y las fallas técnicas como error.
- **Stock:** no se edita en el formulario; se ajusta sumando o restando sobre el valor actual, sin pisar préstamos concurrentes.
- **Seguridad:** contraseñas con hash SHA-256, `PreparedStatement` y credenciales fuera del repositorio.
- **Reglas en el modelo y en la BD:** `Validador` + `NOT NULL`, `UNIQUE`, `CHECK` y claves foráneas.

---

## 📝 Diferencias respecto de la pauta

- **Esquema ajustado** (aprobado por el docente): `fecha_devolucion_real`, `NOT NULL`, `UNIQUE` en categoría y correo, `CHECK` de stock y contraseñas con hash. `fecha_devolucion` es el vencimiento.
- **Estudiante:** solo consulta; los préstamos los registra el bibliotecario (confirmado por el docente).
- **Historial:** consulta de préstamos, sin tabla adicional (confirmado por el docente).

## 🚀 Mejoras identificadas

- Editorial como tabla propia, igual que categoría.
- Filtros por estudiante y libro en la pantalla de préstamos.
- Búsqueda al escribir en los combos.
- Recordar el último usuario en el login.

---

## 🛠️ Requisitos

- **Java 21 o superior** · **MySQL 8.0.16 o superior** · Connector/J incluido.

---

## ⚙️ Instrucciones de ejecución

### 1. Base de datos

1. Con el servidor MySQL en ejecución, ejecuta en MySQL Workbench, **en este orden**:
    - `sql/PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql`
    - `sql/PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql`
2. Si ya existe una base `biblioteca`, elimínala antes con `DROP DATABASE biblioteca;`: el esquema tiene ajustes.

### 2. Opción A: ejecutable (carpeta de entrega)

1. En la carpeta del JAR, copia `db.properties.example` como `db.properties` y completa `db.password`.
2. Haz doble clic en `iniciar.bat`, o desde una terminal en esa carpeta:

```bash
java -jar biblioteca-escolar.jar
```

### 3. Opción B: desde el código fuente

```bash
# 1. Clona el repositorio
git clone https://github.com/Psmithortiz/biblioteca-escolar-java-mysql.git

# 2. Entra a la carpeta del proyecto
cd biblioteca-escolar-java-mysql
```

3. **Configura las credenciales:** copia `db.properties.example` como `db.properties` en la raíz del proyecto y completa `db.password` con la contraseña de tu usuario de MySQL.
4. Abre la carpeta en IntelliJ IDEA (`File → Open`) y configura un JDK 21 o superior.
5. **Agrega el conector JDBC:** `File → Project Structure → Libraries → + → Java` → selecciona `lib/mysql-connector-j-26.7.0.jar`.
6. Con el servidor MySQL en ejecución, ejecuta `Main.java` del paquete `main`.

> `db.properties` se busca en la carpeta desde donde se ejecuta la aplicación: la del JAR en la opción A y la raíz del proyecto en la opción B. Si falta, la aplicación lo indica al iniciar.

---

## 👥 Usuarios de prueba (contraseña `clave123`)

| Rol | RUT | Correo |
|---|---|---|
| Bibliotecario | `12345678-9` | `antonia@correo.cl` |
| Estudiante | `98765432-1` | `carlos@correo.cl` |

---

**Repositorio GitHub:** https://github.com/Psmithortiz/biblioteca-escolar-java-mysql
**Fecha de entrega:** _DD/10/2026_

---

© Duoc UC | Escuela de Informática y Telecomunicaciones | Desarrollo Orientado a Objetos II