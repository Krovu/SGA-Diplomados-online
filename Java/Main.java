import java.io.*;
import java.util.*;

class Persona {
    String cedula, nombre, email;

    public Persona(String c, String n, String e) {
        this.cedula = c;
        this.nombre = n;
        this.email = e;
    }
}

class Profesor extends Persona {
    String especialidad, materia;

    public Profesor(String c, String n, String e, String esp, String mat) {
        super(c, n, e);
        this.especialidad = esp;
        this.materia = mat;
    }
}

abstract class ProgramaAcademico {
    String nombrePrograma;

    public ProgramaAcademico() {
        this.nombrePrograma = this.getClass().getSimpleName();
    }

    public abstract boolean esAprobado(double promedio);
}

class Curso extends ProgramaAcademico {
    @Override
    public boolean esAprobado(double promedio) { return promedio >= 10.0; }
}

class Diplomado extends ProgramaAcademico {
    @Override
    public boolean esAprobado(double promedio) { return promedio >= 14.0; }
}

class Bootcamp extends ProgramaAcademico {
    @Override
    public boolean esAprobado(double promedio) { return promedio >= 16.0; }
}

class Alumno extends Persona {
    ProgramaAcademico programa;
    List<Double> notas;

    public Alumno(String c, String n, String e, ProgramaAcademico prog) {
        super(c, n, e);
        this.programa = prog;
        this.notas = new ArrayList<>();
    }

    public void registrarNota(double nota) {
        this.notas.add(nota);
    }

    public double calcularPromedio() {
        if (notas.isEmpty()) return 0.0;
        double suma = 0;
        for (double n : notas) suma += n;
        return suma / 3.0;
    }

    public boolean estaAprobado() {
        return programa.esAprobado(calcularPromedio());
    }
}

class NotaRegistro {
    Alumno alumno;
    double nota;

    public NotaRegistro(Alumno a, double n) {
        this.alumno = a;
        this.nota = n;
    }
}

class SGA {
    private final List<Alumno> alumnos = new ArrayList<>();
    private final List<Profesor> profesores = new ArrayList<>();
    private final Stack<NotaRegistro> pilaNotas = new Stack<>();
    private final Scanner sc = new Scanner(System.in);

    public SGA() {
        cargarDatos();
    }

    private void cargarDatos() {
        alumnos.clear();
        profesores.clear();

        try (BufferedReader br = new BufferedReader(new FileReader("Profesores.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] d = linea.split(",");
                if (d.length >= 5) {
                    profesores.add(new Profesor(d[0].trim(), d[1].trim(), d[2].trim(), d[3].trim(), d[4].trim()));
                }
            }
        } catch (IOException ignored) {}

        try (BufferedReader br = new BufferedReader(new FileReader("Alumnos.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] d = linea.split(",");
                if (d.length >= 4) {
                    String ced = d[0].trim();
                    String nom = d[1].trim();
                    String email = d[2].trim();
                    String tipo = d[3].trim().toLowerCase();

                    ProgramaAcademico prog = switch (tipo) {
                        case "diplomado" -> new Diplomado();
                        case "bootcamp" -> new Bootcamp();
                        default -> new Curso();
                    };

                    Alumno al = new Alumno(ced, nom, email, prog);
                    for (int i = 4; i < d.length; i++) {
                        try {
                            double v = Double.parseDouble(d[i].trim());
                            if (v >= 0 && v <= 20) al.registrarNota(v);
                        } catch (NumberFormatException ignored) {}
                    }
                    alumnos.add(al);
                }
            }
        } catch (IOException ignored) {}
    }

    public void guardarAlumnos() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("Alumnos.txt"))) {
            for (Alumno a : alumnos) {
                StringBuilder sb = new StringBuilder();
                sb.append(a.cedula).append(",").append(a.nombre).append(",").append(a.email).append(",").append(a.programa.getClass().getSimpleName());
                for (double n : a.notas) sb.append(",").append(n);
                pw.println(sb.toString());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar.");
        }
    }

    public void guardarProfesores() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("Profesores.txt"))) {
            for (Profesor p : profesores) {
                pw.println(p.cedula + "," + p.nombre + "," + p.email + "," + p.especialidad + "," + p.materia);
            }
        } catch (IOException e) {
            System.out.println("Error al guardar.");
        }
    }

    public void registrarProfesor() {
        cargarDatos();
        System.out.println("\n--- REGISTRAR PROFESOR ---");
        System.out.print("Cedula: "); String ced = sc.nextLine().trim();
        for (Profesor p : profesores) {
            if (p.cedula.equals(ced)) {
                System.out.println("Ya existe un profe con esa cedula.");
                return;
            }
        }
        System.out.print("Nombre: "); String nom = sc.nextLine().trim();
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Especialidad: "); String esp = sc.nextLine().trim();
        System.out.print("Materia: "); String mat = sc.nextLine().trim();

        profesores.add(new Profesor(ced, nom, email, esp, mat));
        guardarProfesores();
        System.out.println("Profesor registrado.");
    }

    public void registrarAlumno() {
        cargarDatos();
        System.out.println("\n--- REGISTRAR ALUMNO ---");
        System.out.print("Cedula: "); String ced = sc.nextLine().trim();
        for (Alumno a : alumnos) {
            if (a.cedula.equals(ced)) {
                System.out.println("Esa cedula ya esta registrada.");
                return;
            }
        }
        System.out.print("Nombre: "); String nom = sc.nextLine().trim();
        System.out.print("Email: "); String email = sc.nextLine().trim();

        ProgramaAcademico prog = null;
        while (prog == null) {
            System.out.print("Tipo (curso / diplomado / bootcamp): ");
            String tipo = sc.nextLine().trim().toLowerCase();
            switch (tipo) {
                case "curso" -> prog = new Curso();
                case "diplomado" -> prog = new Diplomado();
                case "bootcamp" -> prog = new Bootcamp();
                default -> System.out.println("Invalido, intenta de nuevo.");
            }
        }

        alumnos.add(new Alumno(ced, nom, email, prog));
        guardarAlumnos();
        System.out.println("Alumno registrado.");
    }

    public void cargarNotas() {
        cargarDatos();
        System.out.println("\n--- CARGAR NOTAS (0 a 20) ---");
        System.out.print("Cedula del alumno: ");
        String ced = sc.nextLine().trim();

        Alumno al = null;
        for (Alumno a : alumnos) {
            if (a.cedula.equals(ced)) {
                al = a;
                break;
            }
        }
        if (al == null) {
            System.out.println("No se encontro el alumno.");
            return;
        }

        int cant = al.notas.size();
        while (cant < 3) {
            System.out.print("Nota " + (cant + 1) + " (Enter para salir): ");
            String txt = sc.nextLine().trim();
            if (txt.isEmpty()) break;
            try {
                double val = Double.parseDouble(txt);
                if (val >= 0 && val <= 20) {
                    al.registrarNota(val);
                    pilaNotas.push(new NotaRegistro(al, val));
                    cant++;
                } else {
                    System.out.println("Debe ser de 0 a 20.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada no valida.");
            }
        }
        guardarAlumnos();
        System.out.println("Notas actualizadas.");
    }

    public void deshacerNota() {
        cargarDatos();
        if (pilaNotas.isEmpty()) {
            System.out.println("No hay notas para deshacer.");
            return;
        }
        NotaRegistro reg = pilaNotas.pop();
        Alumno al = null;
        for (Alumno a : alumnos) {
            if (a.cedula.equals(reg.alumno.cedula)) {
                al = a;
                break;
            }
        }
        if (al != null && al.notas.contains(reg.nota)) {
            al.notas.remove(reg.nota);
            guardarAlumnos();
            System.out.println("Nota eliminada correctamente.");
        }
    }

    public void gestionarCertificados() {
        cargarDatos();
        List<Alumno> aprobados = new ArrayList<>();
        for (Alumno a : alumnos) {
            if (a.estaAprobado()) aprobados.add(a);
        }

        if (aprobados.isEmpty()) {
            System.out.println("No hay alumnos aprobados.");
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("certificados_pendientes.txt"))) {
            int i = 1;
            for (Alumno c : aprobados) {
                pw.println(i + "," + c.cedula + "," + c.nombre + "," + c.programa.nombrePrograma + "," + String.format(Locale.US, "%.2f", c.calcularPromedio()) + ",EMITIDO");
                i++;
            }
        } catch (IOException ignored) {}

        System.out.println("Emitiendo certificados (" + aprobados.size() + "):");
        for (Alumno c : aprobados) {
            System.out.println("- " + c.nombre + " (" + c.programa.nombrePrograma + ") | Prom: " + String.format(Locale.US, "%.2f", c.calcularPromedio()));
        }
    }

    public void mostrarReporte() {
        cargarDatos();
        System.out.println("\n--- PROFESORES ---");
        if (profesores.isEmpty()) System.out.println("Vacio.");
        for (Profesor p : profesores) {
            System.out.println(p.cedula + " | " + p.nombre + " | " + p.materia);
        }

        System.out.println("\n--- ALUMNOS ---");
        if (alumnos.isEmpty()) System.out.println("Vacio.");
        for (Alumno a : alumnos) {
            double prom = a.calcularPromedio();
            String est = a.estaAprobado() ? "Aprobado" : "Reprobado";
            System.out.println(a.cedula + " | " + a.nombre + " | " + a.programa.nombrePrograma + " | Notas: " + a.notas + " | Prom: " + String.format(Locale.US, "%.2f", prom) + " (" + est + ")");
        }
    }

    public void menu() {
        String op = "";
        while (!op.equals("7")) {
            System.out.println("\n*** MENU ***");
            System.out.println("1. Registrar Profesor");
            System.out.println("2. Registrar Alumno");
            System.out.println("3. Cargar Notas");
            System.out.println("4. Deshacer Nota");
            System.out.println("5. Emitir Certificados");
            System.out.println("6. Ver Reporte");
            System.out.println("7. Salir");
            System.out.print("Opcion: ");
            op = sc.nextLine().trim();

            switch (op) {
                case "1" -> registrarProfesor();
                case "2" -> registrarAlumno();
                case "3" -> cargarNotas();
                case "4" -> deshacerNota();
                case "5" -> gestionarCertificados();
                case "6" -> mostrarReporte();
                case "7" -> System.out.println("Saliendo...");
                default -> System.out.println("Opcion no valida.");
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        new SGA().menu();
    }
}