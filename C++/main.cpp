#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <stack>
#include <string>
#include <algorithm>
#include <iomanip>

using namespace std;

// --- CLASES DE DOMINIO ---
class Persona {
public:
    string cedula;
    string nombre;
    string email;

    Persona(string c, string n, string e) : cedula(c), nombre(n), email(e) {}
    virtual ~Persona() {}
};

class Profesor : public Persona {
public:
    string especialidad;
    string materia;

    Profesor(string c, string n, string e, string esp, string mat)
        : Persona(c, n, e), especialidad(esp), materia(mat) {}
};

// --- POLIMORFISMO REGLAS DE APROBACIÓN ---
class ProgramaAcademico {
public:
    string nombrePrograma;

    ProgramaAcademico(string nombre) : nombrePrograma(nombre) {}
    virtual ~ProgramaAcademico() {}
    virtual bool esAprobado(double promedio, const vector<double>& notas) = 0;
};

class Curso : public ProgramaAcademico {
public:
    Curso() : ProgramaAcademico("Curso") {}
    bool esAprobado(double promedio, const vector<double>& notas) override {
        return promedio >= 10.0;
    }
};

class Diplomado : public ProgramaAcademico {
public:
    Diplomado() : ProgramaAcademico("Diplomado") {}
    bool esAprobado(double promedio, const vector<double>& notas) override {
        return promedio >= 14.0;
    }
};

class Bootcamp : public ProgramaAcademico {
public:
    Bootcamp() : ProgramaAcademico("Bootcamp") {}
    bool esAprobado(double promedio, const vector<double>& notas) override {
        if (notas.size() < 3) return false;
        for (double n : notas) {
            if (n < 14.0) return false;
        }
        return true;
    }
};

class Alumno : public Persona {
public:
    ProgramaAcademico* programa;
    vector<double> notas;

    Alumno(string c, string n, string e, ProgramaAcademico* prog)
        : Persona(c, n, e), programa(prog) {}

    ~Alumno() {
        delete programa; // Evita fuga de memoria del polimorfismo
    }

    void registrarNota(double nota) {
        notas.push_back(nota);
    }

    double calcularPromedio() const {
        if (notas.empty()) return 0.0;
        double suma = 0;
        for (double n : notas) suma += n;
        return suma / 3.0;
    }

    bool estaAprobado() const {
        return programa->esAprobado(calcularPromedio(), notas);
    }
};

class NotaRegistro {
public:
    Alumno* alumno;
    double nota;

    NotaRegistro(Alumno* a, double n) : alumno(a), nota(n) {}
};

// --- SISTEMA GESTIÓN ACADÉMICA (SGA) ---
class SGA {
private:
    vector<Alumno*> alumnos;
    vector<Profesor*> profesores;
    stack<NotaRegistro> pilaNotas;

    void limpiarMemoria() {
        for (Alumno* a : alumnos) {
            delete a;
        }
        alumnos.clear();

        for (Profesor* p : profesores) {
            delete p;
        }
        profesores.clear();
    }

public:
    SGA() {
        cargarDatos();
    }

    ~SGA() {
        limpiarMemoria(); // Libera toda la memoria dinámica al cerrar el programa
    }

    void cargarDatos() {
        limpiarMemoria();

        // Cargar Profesores
        ifstream archProf("Profesores.txt");
        if (archProf.is_open()) {
            string linea;
            while (getline(archProf, linea)) {
                if (linea.empty()) continue;
                stringstream ss(linea);
                string ced, nom, email, esp, mat;
                if (getline(ss, ced, ',') && getline(ss, nom, ',') &&
                    getline(ss, email, ',') && getline(ss, esp, ',') && getline(ss, mat, ',')) {
                    profesores.push_back(new Profesor(ced, nom, email, esp, mat));
                }
            }
            archProf.close();
        }

        // Cargar Alumnos
        ifstream archAlum("Alumnos.txt");
        if (archAlum.is_open()) {
            string linea;
            while (getline(archAlum, linea)) {
                if (linea.empty()) continue;
                stringstream ss(linea);
                string ced, nom, email, tipo;
                if (getline(ss, ced, ',') && getline(ss, nom, ',') &&
                    getline(ss, email, ',') && getline(ss, tipo, ',')) {
                    
                    string tLower = tipo;
                    transform(tLower.begin(), tLower.end(), tLower.begin(), ::tolower);

                    ProgramaAcademico* prog = nullptr;
                    if (tLower == "diplomado") prog = new Diplomado();
                    else if (tLower == "bootcamp") prog = new Bootcamp();
                    else prog = new Curso();

                    Alumno* al = new Alumno(ced, nom, email, prog);

                    string notaStr;
                    while (getline(ss, notaStr, ',')) {
                        try {
                            double v = stod(notaStr);
                            if (v >= 0.0 && v <= 20.0) {
                                al->registrarNota(v);
                            }
                        } catch (...) {}
                    }
                    alumnos.push_back(al);
                }
            }
            archAlum.close();
        }
    }

    void guardarAlumnos() {
        ofstream pw("Alumnos.txt");
        if (pw.is_open()) {
            for (Alumno* a : alumnos) {
                pw << a->cedula << "," << a->nombre << "," << a->email << "," << a->programa->nombrePrograma;
                for (double n : a->notas) {
                    pw << "," << n;
                }
                pw << "\n";
            }
            pw.close();
        }
    }

    void guardarProfesores() {
        ofstream pw("Profesores.txt");
        if (pw.is_open()) {
            for (Profesor* p : profesores) {
                pw << p->cedula << "," << p->nombre << "," << p->email << "," << p->especialidad << "," << p->materia << "\n";
            }
            pw.close();
        }
    }

    void registrarProfesor() {
        cargarDatos();
        cout << "\n--- REGISTRAR PROFESOR ---\n";
        string ced, nom, email, esp, mat;
        cout << "Cedula: "; getline(cin, ced);

        for (Profesor* p : profesores) {
            if (p->cedula == ced) {
                cout << "Ya existe un profesor con esa cedula.\n";
                return;
            }
        }

        cout << "Nombre: "; getline(cin, nom);
        cout << "Email: "; getline(cin, email);
        cout << "Especialidad: "; getline(cin, esp);
        cout << "Materia: "; getline(cin, mat);

        profesores.push_back(new Profesor(ced, nom, email, esp, mat));
        guardarProfesores();
        cout << "Profesor registrado.\n";
    }

    void registrarAlumno() {
        cargarDatos();
        cout << "\n--- REGISTRAR ALUMNO ---\n";
        string ced, nom, email, tipo;
        cout << "Cedula: "; getline(cin, ced);

        for (Alumno* a : alumnos) {
            if (a->cedula == ced) {
                cout << "Esa cedula ya esta registrada.\n";
                return;
            }
        }

        cout << "Nombre: "; getline(cin, nom);
        cout << "Email: "; getline(cin, email);

        ProgramaAcademico* prog = nullptr;
        while (prog == nullptr) {
            cout << "Tipo (curso / diplomado / bootcamp): ";
            getline(cin, tipo);
            string tLower = tipo;
            transform(tLower.begin(), tLower.end(), tLower.begin(), ::tolower);

            if (tLower == "curso") prog = new Curso();
            else if (tLower == "diplomado") prog = new Diplomado();
            else if (tLower == "bootcamp") prog = new Bootcamp();
            else cout << "Invalido, intenta de nuevo.\n";
        }

        alumnos.push_back(new Alumno(ced, nom, email, prog));
        guardarAlumnos();
        cout << "Alumno registrado.\n";
    }

    void cargarNotas() {
        cargarDatos();
        cout << "\n--- CARGAR NOTAS (0 a 20) ---\n";
        string ced;
        cout << "Cedula del alumno: "; getline(cin, ced);

        Alumno* al = nullptr;
        for (Alumno* a : alumnos) {
            if (a->cedula == ced) {
                al = a;
                break;
            }
        }

        if (al == nullptr) {
            cout << "No se encontro el alumno.\n";
            return;
        }

        int cant = al->notas.size();
        while (cant < 3) {
            string txt;
            cout << "Nota " << (cant + 1) << " (Enter para salir): ";
            getline(cin, txt);
            if (txt.empty()) break;
            try {
                double val = stod(txt);
                if (val >= 0.0 && val <= 20.0) {
                    al->registrarNota(val);
                    pilaNotas.push(NotaRegistro(al, val));
                    cant++;
                } else {
                    cout << "Debe ser de 0 a 20.\n";
                }
            } catch (...) {
                cout << "Entrada no valida.\n";
            }
        }
        guardarAlumnos();
        cout << "Notas actualizadas.\n";
    }

    void deshacerNota() {
        if (pilaNotas.empty()) {
            cout << "No hay notas para deshacer.\n";
            return;
        }
        NotaRegistro reg = pilaNotas.top();
        pilaNotas.pop();

        Alumno* al = nullptr;
        for (Alumno* a : alumnos) {
            if (a->cedula == reg.alumno->cedula) {
                al = a;
                break;
            }
        }

        if (al != nullptr) {
            auto it = find(al->notas.begin(), al->notas.end(), reg.nota);
            if (it != al->notas.end()) {
                al->notas.erase(it);
                guardarAlumnos();
                cout << "Nota eliminada correctamente.\n";
            } else {
                cout << "La nota ya no se encuentra registrada en el alumno.\n";
            }
        } else {
            cout << "El alumno asociado a la nota ya no existe.\n";
        }
    }

    void gestionarCertificados() {
        cargarDatos();
        vector<Alumno*> aprobados;
        for (Alumno* a : alumnos) {
            if (a->estaAprobado()) aprobados.push_back(a);
        }

        if (aprobados.empty()) {
            cout << "No hay alumnos aprobados.\n";
            return;
        }

        ofstream pw("certificados_pendientes.txt");
        if (pw.is_open()) {
            pw << "=========================================\n\n";
            pw << "REPORTE DE CERTIFICADOS PENDIENTES\n\n";
            pw << "=========================================\n\n";
            pw << "Total de graduandos en cola: " << aprobados.size() << "\n\n";
            int i = 1;
            for (Alumno* c : aprobados) {
                pw << i << ". [" << c->cedula << "] " << c->nombre << "\n\n";
                pw << "- Programa: " << c->programa->nombrePrograma << "\n\n";
                pw << "- Promedio Final: " << fixed << setprecision(1) << c->calcularPromedio() << "\n\n";
                pw << "- Estatus: APROBADO\n\n";
                i++;
            }
            pw.close();
        }

        cout << "Se generó el reporte para " << aprobados.size() << " alumnos aprobados en 'certificados_pendientes.txt'.\n";
    }

    void mostrarReporte() {
        cargarDatos();
        cout << "\n--- PROFESORES ---\n";
        if (profesores.empty()) cout << "Vacio.\n";
        for (Profesor* p : profesores) {
            cout << p->cedula << " | " << p->nombre << " | " << p->materia << "\n";
        }

        cout << "\n--- ALUMNOS ---\n";
        if (alumnos.empty()) cout << "Vacio.\n";
        for (Alumno* a : alumnos) {
            double prom = a->calcularPromedio();
            string est = a->estaAprobado() ? "Aprobado" : "Reprobado";
            cout << a->cedula << " | " << a->nombre << " | " << a->programa->nombrePrograma << " | Notas: [";
            for (size_t i = 0; i < a->notas.size(); ++i) {
                cout << a->notas[i] << (i + 1 < a->notas.size() ? ", " : "");
            }
            cout << "] | Prom: " << fixed << setprecision(1) << prom << " (" << est << ")\n";
        }
    }

    void menu() {
        string op = "";
        while (op != "7") {
            cout << "\n*** MENU ***\n";
            cout << "1. Registrar Profesor\n";
            cout << "2. Registrar Alumno\n";
            cout << "3. Cargar Notas\n";
            cout << "4. Deshacer Nota\n";
            cout << "5. Emitir Certificados\n";
            cout << "6. Ver Reporte\n";
            cout << "7. Salir\n";
            cout << "Opcion: ";
            getline(cin, op);

            if (op == "1") registrarProfesor();
            else if (op == "2") registrarAlumno();
            else if (op == "3") cargarNotas();
            else if (op == "4") deshacerNota();
            else if (op == "5") gestionarCertificados();
            else if (op == "6") mostrarReporte();
            else if (op == "7") cout << "Saliendo...\n";
            else cout << "Opcion no valida.\n";
        }
    }
};

int main() {
    SGA sistema;
    sistema.menu();
    return 0;
}