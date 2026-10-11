import java.io.*;
import java.util.*;

class Persona {
    protected String cedula;
    protected String nombre;
    protected String email;

    public Persona(String cedula, String nombre, String email) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.email = email;
    }
}

class Profesor extends Persona {
    String especialidad;
    String materia;

    public Profesor(String cedula, String nombre, String email, String especialidad, String materia) {
        super(cedula, nombre, email);
        this.especialidad = especialidad;
        this.materia = materia;
    }
}

// --- REGLAS DE APROBACIÓN ---
abstract class ProgramaAcademico {
    String nombrePrograma;

    public ProgramaAcademico() {
        this.nombrePrograma = this.getClass().getSimpleName();
    }

    public abstract boolean esAprobado(double promedio);
}

class Curso extends ProgramaAcademico {
    @Override
    public boolean esAprobado(double promedio) {
        return promedio >= 10.0;
    }
}

class Diplomado extends ProgramaAcademico {
    @Override
    public boolean esAprobado(double promedio) {
        return promedio >= 14.0;
    }
}

class Bootcamp extends ProgramaAcademico {
    @Override
    public boolean esAprobado(double promedio) {
        return promedio >= 14.0;
    }
}

class Alumno extends Persona {
    ProgramaAcademico programa;
    List<Double> notas;

    public Alumno(String cedula, String nombre, String email, ProgramaAcademico programa) {
        super(cedula, nombre, email);
        this.programa = programa;
        this.notas = new ArrayList<>();
    }

    public void registrarNota(double nota) {
        this.notas.add(nota);
    }

    public double calcularPromedio() {
        double suma = 0;
        for (double n : notas) {
            suma += n;
        }
        // Las notas faltantes cuentan como 0, por lo que el denominador siempre es 3 fijo
        return suma / 3.0;
    }

    public boolean estaAprobado() {
        return programa.esAprobado(calcularPromedio());
    }
}

class NotaRegistro {
    Alumno alumno;
    double nota;

    public NotaRegistro(Alumno alumno, double nota) {
        this.alumno = alumno;
        this.nota = nota;
    }
}

class SGA {
    private final List<Alumno> alumnos = new ArrayList<>();
    private final List<Profesor> profesores = new ArrayList<>();
    private final Stack<NotaRegistro> pilaNotas = new Stack<>();
    private final Scanner scanner = new Scanner(System.in);

    public SGA() {
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        alumnos.clear();
        profesores.clear();

        // Cargar Profesores: cedula,nombre,email,especialidad,materia
        try (BufferedReader br = new BufferedReader(new FileReader("Profesores.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] datos = linea.split(",");
                if (datos.length >= 5) {
                    profesores.add(new Profesor(datos[0].trim(), datos[1].trim(), datos[2].trim(), datos[3].trim(), datos[4].trim()));
                }
            }
        } catch (IOException e) {
            // Archivo inexistente en la primera ejecución
        }

        // Cargar Alumnos: cedula,nombre,email,tipo_programa,nota1,nota2...
        try (BufferedReader br = new BufferedReader(new FileReader("Alumnos.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] datos = linea.split(",");
                if (datos.length >= 4) {
                    String ced = datos[0].trim();
                    String nom = datos[1].trim();
                    String email = datos[2].trim();
                    String tipo = datos[3].trim().toLowerCase();

                    ProgramaAcademico prog = switch (tipo) {
                        case "diplomado" -> new Diplomado();
                        case "bootcamp" -> new Bootcamp();
                        default -> new Curso();
                    };

                    Alumno al = new Alumno(ced, nom, email, prog);

                    for (int i = 4; i < datos.length; i++) {
                        try {
                            al.registrarNota(Double.parseDouble(datos[i].trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                    alumnos.add(al);
                }
            }
        } catch (IOException e) {
            // Archivo inexistente en la primera ejecución
        }
    }

    public void guardarAlumnosTxt() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("Alumnos.txt"))) {
            for (Alumno a : alumnos) {
                StringBuilder sb = new StringBuilder();
                sb.append(a.cedula).append(",")
                  .append(a.nombre).append(",")
                  .append(a.email).append(",")
                  .append(a.programa.getClass().getSimpleName());
                for (double n : a.notas) {
                    sb.append(",").append(n);
                }
                pw.println(sb.toString());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar Alumnos.txt: " + e.getMessage());
        }
    }

    public void guardarProfesoresTxt() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("Profesores.txt"))) {
            for (Profesor p : profesores) {
                pw.println(p.cedula + "," + p.nombre + "," + p.email + "," + p.especialidad + "," + p.materia);
            }
        } catch (IOException e) {
            System.out.println("Error al guardar Profesores.txt: " + e.getMessage());
        }
    }

    public void registrarProfesor() {
        cargarDatosIniciales();
        System.out.println("\n--- Registro Profesor ---");
        System.out.print("Cedula: "); String ced = scanner.nextLine().trim();
        System.out.print("Nombre: "); String nom = scanner.nextLine().trim();
        System.out.print("Email: "); String email = scanner.nextLine().trim();
        System.out.print("Especialidad: "); String esp = scanner.nextLine().trim();
        System.out.print("Materia: "); String mat = scanner.nextLine().trim();

        profesores.add(new Profesor(ced, nom, email, esp, mat));
        guardarProfesoresTxt();
        System.out.println("Profesor registrado y guardado en archivo.");
    }

    public void registrarAlumno() {
        cargarDatosIniciales();
        System.out.println("\n--- Registro Alumno ---");
        System.out.print("Cedula: "); String ced = scanner.nextLine().trim();
        System.out.print("Nombre: "); String nom = scanner.nextLine().trim();
        System.out.print("Email: "); String email = scanner.nextLine().trim();

        ProgramaAcademico prog = null;
        while (prog == null) {
            System.out.print("Tipo de programa (Curso / Diplomado / Bootcamp): ");
            String tipo = scanner.nextLine().trim().toLowerCase();
            switch (tipo) {
                case "curso" -> prog = new Curso();
                case "diplomado" -> prog = new Diplomado();
                case "bootcamp" -> prog = new Bootcamp();
                default -> System.out.println("Error: Tipo no valido. Debe ser 'Curso', 'Diplomado' o 'Bootcamp'. Intente de nuevo.\n");
            }
        }

        Alumno al = new Alumno(ced, nom, email, prog);
        alumnos.add(al);
        guardarAlumnosTxt();
        System.out.println("Alumno registrado exitosamente en " + prog.nombrePrograma + ".");
    }

    public void cargarNotas() {
        cargarDatosIniciales();
        System.out.println("\n--- Cargar Notas ---");
        System.out.print("Ingrese la cedula del alumno: ");
        String ced = scanner.nextLine().trim();

        Alumno alumnoSel = null;
        for (Alumno a : alumnos) {
            if (a.cedula.equals(ced)) {
                alumnoSel = a;
                break;
            }
        }

        if (alumnoSel == null) {
            System.out.println("Alumno no encontrado.");
            return;
        }

        System.out.println("Cargando notas para " + alumnoSel.nombre + " (" + alumnoSel.programa.nombrePrograma + "). Enter vacio para salir.");
        int cant = alumnoSel.notas.size();
        while (cant < 3) {
            System.out.print("Nota " + (cant + 1) + ": ");
            String entrada = scanner.nextLine().trim();
            if (entrada.isEmpty()) break;
            try {
                double num = Double.parseDouble(entrada);
                alumnoSel.registrarNota(num);
                pilaNotas.push(new NotaRegistro(alumnoSel, num));
                cant++;
            } catch (NumberFormatException e) {
                System.out.println("Nota invalida, ingrese un numero.");
            }
        }
        guardarAlumnosTxt();
        System.out.println("Notas actualizadas en disco.");
    }

    public void deshacerNota() {
        cargarDatosIniciales();
        System.out.println("\n--- Deshacer Nota ---");
        if (pilaNotas.isEmpty()) {
            System.out.println("No hay notas registradas en esta sesion para eliminar.");
            return;
        }

        NotaRegistro reg = pilaNotas.pop();
        Alumno alumnoActual = null;
        for (Alumno a : alumnos) {
            if (a.cedula.equals(reg.alumno.cedula)) {
                alumnoActual = a;
                break;
            }
        }

        if (alumnoActual != null && alumnoActual.notas.contains(reg.nota)) {
            alumnoActual.notas.remove(reg.nota);
            guardarAlumnosTxt();
            System.out.println("Se elimino la nota " + reg.nota + " de " + alumnoActual.nombre + " y se actualizo Alumnos.txt.");
        }
    }

    public void gestionarCertificados() {
        cargarDatosIniciales();
        System.out.println("\n--- EMISIÓN MASIVA DE CERTIFICADOS ---");
        if (alumnos.isEmpty()) {
            System.out.println("Sin alumnos cargados.");
            return;
        }

        List<Alumno> cola = new ArrayList<>();
        for (Alumno a : alumnos) {
            if (a.estaAprobado()) {
                cola.add(a);
            }
        }

        if (cola.isEmpty()) {
            System.out.println("No hay alumnos que cumplan las condiciones de aprobacion de su programa.");
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("certificados_pendientes.txt"))) {
            pw.println("--- CERTIFICADOS PROCESADOS Y EMITIDOS ---");
            int i = 1;
            for (Alumno c : cola) {
                pw.println(i + ", Cedula: " + c.cedula + ", " + c.nombre + ", Prog: " + c.programa.nombrePrograma + ", Prom: " + String.format(Locale.US, "%.2f", c.calcularPromedio()) + ", Estatus: EMITIDO");
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo de certificados: " + e.getMessage());
        }

        System.out.println("Se encontraron " + cola.size() + " alumno(s) aprobados.");
        int emitidos = 0;
        while (!cola.isEmpty()) {
            Alumno emitido = cola.remove(0);
            System.out.println("-> Certificado emitido: " + emitido.nombre + " | " + emitido.programa.nombrePrograma + " | Promedio: " + String.format(Locale.US, "%.2f", emitido.calcularPromedio()));
            emitidos++;
        }
        System.out.println("\nProceso finalizado. Se emitieron " + emitidos + " certificado(s).");
    }

    public void mostrarReporte() {
        cargarDatosIniciales();
        System.out.println("\n--- REPORTE PROFESORES ---");
        if (profesores.isEmpty()) {
            System.out.println("Sin profesores registrados.");
        } else {
            for (Profesor p : profesores) {
                System.out.println("Cedula: " + p.cedula + " | Nombre: " + p.nombre + " | Materia: " + p.materia);
            }
        }

        System.out.println("\n--- REPORTE ALUMNOS ---");
        if (alumnos.isEmpty()) {
            System.out.println("Sin alumnos registrados.");
        } else {
            for (Alumno a : alumnos) {
                double prom = a.calcularPromedio();
                String est = a.estaAprobado() ? "Aprobado" : "Reprobado";
                System.out.println("Cedula: " + a.cedula + " | Nombre: " + a.nombre + " | Programa: " + a.programa.nombrePrograma + " | Notas: " + a.notas + " | Promedio: " + String.format(Locale.US, "%.2f", prom) + " (" + est + ")");
            }
        }
    }

    public void menu() {
        String opc = "";
        while (!opc.equals("7")) {
            System.out.println("\n*** MENU SGA ***");
            System.out.println("1. Registrar Profesor");
            System.out.println("2. Registrar Alumno");
            System.out.println("3. Cargar Notas");
            System.out.println("4. Deshacer Ultima Nota");
            System.out.println("5. Emitir Todos los Certificados");
            System.out.println("6. Ver Reporte");
            System.out.println("7. Salir");
            System.out.print("Opcion: ");
            opc = scanner.nextLine().trim();

            switch (opc) {
                case "1" -> registrarProfesor();
                case "2" -> registrarAlumno();
                case "3" -> cargarNotas();
                case "4" -> deshacerNota();
                case "5" -> gestionarCertificados();
                case "6" -> mostrarReporte();
                case "7" -> System.out.println("Saliendo del programa...");
                default -> System.out.println("Opcion no valida.");
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        SGA sistema = new SGA();
        sistema.menu();
    }
}