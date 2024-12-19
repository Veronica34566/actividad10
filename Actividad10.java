import java.io.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

// Clase que implementa la validación de contraseñas y registra el resultado
class ValidadorContraseña implements Runnable {
    private String contraseña;
    private Consumer<String> registrarResultado;  // Usamos una expresión lambda para registrar el resultado

    public ValidadorContraseña(String contraseña, Consumer<String> registrarResultado) {
        this.contraseña = contraseña;
        this.registrarResultado = registrarResultado;
    }

    @Override
    public void run() {
        String resultado = (validarContraseña(contraseña))
                ? "La contraseña '" + contraseña + "' es válida."
                : "La contraseña '" + contraseña + "' no es válida.";

        registrarResultado.accept(resultado);  // Llamamos al consumidor (lambda) para registrar el resultado
    }

    // Método que valida la contraseña utilizando expresiones regulares
    private boolean validarContraseña(String contraseña) {
        // Expresión regular para validar los requisitos de la contraseña
        String patron = "^(?=.*[A-Z].*[A-Z])(?=.*[a-z].*[a-z].*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";

        // Crear el patrón y el matcher
        Pattern pattern = Pattern.compile(patron);
        Matcher matcher = pattern.matcher(contraseña);

        return matcher.matches();
    }
}

public class Actividad10 {
    public static void main(String[] args) throws InterruptedException, IOException {
        // Lista de contraseñas a validar
        String[] contraseñas = {
                "Abcdef1@",
                "12345@Axyz",
                "P@ssw0rd123",
                "short",
                "Good@Password1"
        };

        // Archivo de registro donde se guardarán los resultados
        String archivoRegistro = "resultados_validacion.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(archivoRegistro, true));

        // Definir la expresión lambda para registrar los resultados en el archivo
        Consumer<String> registrarResultado = resultado -> {
            try {
                writer.write(resultado);
                writer.newLine();  // Escribimos cada resultado en una nueva línea
            } catch (IOException e) {
                System.out.println("Error al escribir en el archivo de registro: " + e.getMessage());
            }
        };

        // Crear un pool de hilos para ejecutar la validación concurrentemente
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (String contraseña : contraseñas) {
            ValidadorContraseña validador = new ValidadorContraseña(contraseña, registrarResultado);
            executor.submit(validador);  // Enviar cada validación a un hilo del pool
        }

        // Apagar el executor después de que todas las tareas se completen
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);  // Esperar un minuto para que finalicen todos los hilos

        // Cerrar el archivo de registro
        writer.close();
        System.out.println("Resultados de la validación guardados en: " + archivoRegistro);
    }
}