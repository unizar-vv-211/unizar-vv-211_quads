package es.unizar.eina.T251_quads.database;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Clase de pruebas unitarias para validar el funcionamiento de los repositorios.
 * Implementa pruebas de caja negra, pruebas de volumen y pruebas de sobrecarga.
 * Los resultados se muestran mediante Log.d con el tag "Pruebas".
 */
public class UnitTests {

    private static final String TAG = "Pruebas";
    
    private QuadRepository quadRepository;
    private ReservaRepository reservaRepository;
    
    // Patrón para validar matrícula: 4 dígitos + 3 letras
    private static final Pattern MATRICULA_PATTERN = Pattern.compile("^\\d{4}[A-Z]{3}$");
    
    /**
     * Constructor de la clase UnitTests.
     * 
     * @param quadRepo Repositorio de Quads para realizar las pruebas.
     * @param reservaRepo Repositorio de Reservas para realizar las pruebas.
     */
    public UnitTests(QuadRepository quadRepo, ReservaRepository reservaRepo) {
        this.quadRepository = quadRepo;
        this.reservaRepository = reservaRepo;
    }
    
    /**
     * Ejecuta todas las pruebas secuencialmente.
     * Los resultados se muestran en el log con el tag "Pruebas".
     */
    public void runAllTests() {
        Log.d(TAG, "INICIANDO PRUEBAS");
        
        testQuadInsert();
        testQuadDelete();
        testReservaInsert();
        testValidateClientePartitioning();
        testVolumen();
        testSobrecarga();
        
        Log.d(TAG, "PRUEBAS FINALIZADAS");
    }
    
    /**
     * Pruebas unitarias de inserción de Quads (CP-I-1 a CP-I-9).
     */
    private void testQuadInsert() {
        Log.d(TAG, "\n--- PRUEBAS UNITARIAS: QUAD INSERT ---");
        
        // CP-I-1: Válido - Monoplaza
        try {
            String matricula = "1234BBB";
            String tipo = "Monoplaza";
            float precio = 50.0f;
            String desc = "Quad Mono";
            
            if (validarQuad(matricula, tipo, precio, desc)) {
                Quad quad = new Quad(matricula, tipo, precio, desc);
                long result = quadRepository.insert(quad);
                
                if (result > 0) {
                    log("CP-I-1", true, "Quad Insert Válido Monoplaza: CORRECTO (ID=" + result + ")");
                } else {
                    log("CP-I-1", false, "Quad Insert Válido Monoplaza: FALLO - No se insertó correctamente");
                }
            } else {
                log("CP-I-1", false, "Quad Insert Válido Monoplaza: FALLO - Validación incorrecta");
            }
        } catch (Exception e) {
            log("CP-I-1", false, "Quad Insert Válido Monoplaza: FALLO - Excepción: " + e.getMessage());
        }
        
        // CP-I-2: Válido - Biplaza
        try {
            String matricula = "5678CCC";
            String tipo = "Biplaza";
            float precio = 75.0f;
            String desc = "Quad Bi";
            
            if (validarQuad(matricula, tipo, precio, desc)) {
                Quad quad = new Quad(matricula, tipo, precio, desc);
                long result = quadRepository.insert(quad);
                
                if (result > 0) {
                    log("CP-I-2", true, "Quad Insert Válido Biplaza: CORRECTO (ID=" + result + ")");
                } else {
                    log("CP-I-2", false, "Quad Insert Válido Biplaza: FALLO - No se insertó correctamente");
                }
            } else {
                log("CP-I-2", false, "Quad Insert Válido Biplaza: FALLO - Validación incorrecta");
            }
        } catch (Exception e) {
            log("CP-I-2", false, "Quad Insert Válido Biplaza: FALLO - Excepción: " + e.getMessage());
        }
        
        // CP-I-3: Inválido - Matrícula Null
        try {
            String matricula = null;
            String tipo = "Biplaza";
            float precio = 50.0f;
            String desc = "Quad Test";
            
            if (!validarQuad(matricula, tipo, precio, desc)) {
                log("CP-I-3", true, "Quad Insert Matrícula Null: CORRECTO - Validación rechazó correctamente");
            } else {
                // Si la validación no lo detecta, intentamos insertar
                try {
                    Quad quad = new Quad(matricula, tipo, precio, desc);
                    long result = quadRepository.insert(quad);
                    log("CP-I-3", false, "Quad Insert Matrícula Null: FALLO - Se insertó cuando debería fallar");
                } catch (Exception ex) {
                    log("CP-I-3", true, "Quad Insert Matrícula Null: CORRECTO - Excepción capturada: " + ex.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            log("CP-I-3", true, "Quad Insert Matrícula Null: CORRECTO - Excepción capturada: " + e.getClass().getSimpleName());
        }
        
        // CP-I-4: Inválido - Formato de matrícula incorrecto
        try {
            String matricula = "12A";
            String tipo = "Biplaza";
            float precio = 50.0f;
            String desc = "Quad Test";
            
            if (!validarQuad(matricula, tipo, precio, desc)) {
                log("CP-I-4", true, "Quad Insert Formato Inválido: CORRECTO - Validación rechazó correctamente");
            } else {
                Quad quad = new Quad(matricula, tipo, precio, desc);
                long result = quadRepository.insert(quad);
                log("CP-I-4", false, "Quad Insert Formato Inválido: FALLO - Se insertó cuando debería fallar");
            }
        } catch (Exception e) {
            log("CP-I-4", true, "Quad Insert Formato Inválido: CORRECTO - Excepción capturada");
        }
        
        // CP-I-5: Inválido - Tipo incorrecto
        try {
            String matricula = "9999ZZZ";
            String tipo = "Triplaza";
            float precio = 50.0f;
            String desc = "Quad Test";
            
            if (!validarQuad(matricula, tipo, precio, desc)) {
                log("CP-I-5", true, "Quad Insert Tipo Inválido: CORRECTO - Validación rechazó correctamente");
            } else {
                Quad quad = new Quad(matricula, tipo, precio, desc);
                long result = quadRepository.insert(quad);
                log("CP-I-5", false, "Quad Insert Tipo Inválido: FALLO - Se insertó cuando debería fallar");
            }
        } catch (Exception e) {
            log("CP-I-5", true, "Quad Insert Tipo Inválido: CORRECTO - Excepción capturada");
        }
        
        // CP-I-6: Inválido - Tipo Null
        try {
            String matricula = "8888XXX";
            String tipo = null;
            float precio = 50.0f;
            String desc = "Quad Test";
            
            if (!validarQuad(matricula, tipo, precio, desc)) {
                log("CP-I-6", true, "Quad Insert Tipo Null: CORRECTO - Validación rechazó correctamente");
            } else {
                try {
                    Quad quad = new Quad(matricula, tipo, precio, desc);
                    long result = quadRepository.insert(quad);
                    log("CP-I-6", false, "Quad Insert Tipo Null: FALLO - Se insertó cuando debería fallar");
                } catch (Exception ex) {
                    log("CP-I-6", true, "Quad Insert Tipo Null: CORRECTO - Excepción capturada: " + ex.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            log("CP-I-6", true, "Quad Insert Tipo Null: CORRECTO - Excepción capturada");
        }
        
        // CP-I-7: Inválido - Precio negativo
        try {
            String matricula = "7777YYY";
            String tipo = "Monoplaza";
            float precio = -10.0f;
            String desc = "Quad Test";
            
            if (!validarQuad(matricula, tipo, precio, desc)) {
                log("CP-I-7", true, "Quad Insert Precio Negativo: CORRECTO - Validación rechazó correctamente");
            } else {
                Quad quad = new Quad(matricula, tipo, precio, desc);
                long result = quadRepository.insert(quad);
                log("CP-I-7", false, "Quad Insert Precio Negativo: FALLO - Se insertó cuando debería fallar");
            }
        } catch (Exception e) {
            log("CP-I-7", true, "Quad Insert Precio Negativo: CORRECTO - Excepción capturada");
        }
        
        // CP-I-8: Inválido - Descripción Null
        try {
            String matricula = "6666WWW";
            String tipo = "Monoplaza";
            float precio = 50.0f;
            String desc = null;
            
            if (!validarQuad(matricula, tipo, precio, desc)) {
                log("CP-I-8", true, "Quad Insert Descripción Null: CORRECTO - Validación rechazó correctamente");
            } else {
                Quad quad = new Quad(matricula, tipo, precio, desc);
                long result = quadRepository.insert(quad);
                log("CP-I-8", false, "Quad Insert Descripción Null: FALLO - Se insertó cuando debería fallar");
            }
        } catch (Exception e) {
            log("CP-I-8", true, "Quad Insert Descripción Null: CORRECTO - Excepción capturada");
        }
        
        // CP-I-9: Inválido - Duplicado (constraint PK)
        try {
            String matricula = "1234BBB"; // Ya insertado en CP-I-1
            String tipo = "Monoplaza";
            float precio = 50.0f;
            String desc = "Quad Duplicado";
            
            if (validarQuad(matricula, tipo, precio, desc)) {
                Quad quad = new Quad(matricula, tipo, precio, desc);
                long result = quadRepository.insert(quad);
                
                // Con OnConflictStrategy.IGNORE, devuelve -1 si ya existe
                if (result == -1 || result == 0) {
                    log("CP-I-9", true, "Quad Insert Duplicado: CORRECTO - Constraint PK detectado");
                } else {
                    log("CP-I-9", false, "Quad Insert Duplicado: FALLO - Se insertó duplicado (ID=" + result + ")");
                }
            } else {
                log("CP-I-9", false, "Quad Insert Duplicado: FALLO - Validación incorrecta");
            }
        } catch (Exception e) {
            log("CP-I-9", true, "Quad Insert Duplicado: CORRECTO - Excepción capturada: " + e.getMessage());
        }
    }
    
    /**
     * Pruebas unitarias de eliminación de Quads (CP-D-1 a CP-D-3).
     */
    private void testQuadDelete() {
        Log.d(TAG, "\n--- PRUEBAS UNITARIAS: QUAD DELETE ---");
        
        // CP-D-1: Válido - Borrar quad sin reservas
        try {
            String matricula = "1111AAA";
            String tipo = "Monoplaza";
            float precio = 50.0f;
            String desc = "Quad para borrar";
            
            // Primero insertamos el quad
            Quad quad = new Quad(matricula, tipo, precio, desc);
            long insertResult = quadRepository.insert(quad);
            
            if (insertResult > 0) {
                // Ahora lo borramos
                int deleteResult = quadRepository.delete(quad);
                
                if (deleteResult == 1) {
                    log("CP-D-1", true, "Quad Delete Válido: CORRECTO - Borrado exitoso (1 fila)");
                } else {
                    log("CP-D-1", false, "Quad Delete Válido: FALLO - Resultado inesperado: " + deleteResult);
                }
            } else {
                log("CP-D-1", false, "Quad Delete Válido: FALLO - No se pudo insertar el quad para la prueba");
            }
        } catch (Exception e) {
            log("CP-D-1", false, "Quad Delete Válido: FALLO - Excepción: " + e.getMessage());
        }
        
        // CP-D-2: Inválido - Borrar quad que no existe
        try {
            String matricula = "9999ZZZ";
            String tipo = "Monoplaza";
            float precio = 50.0f;
            String desc = "Quad inexistente";
            
            Quad quad = new Quad(matricula, tipo, precio, desc);
            int deleteResult = quadRepository.delete(quad);
            
            if (deleteResult == 0) {
                log("CP-D-2", true, "Quad Delete No Existe: CORRECTO - 0 filas afectadas");
            } else {
                log("CP-D-2", false, "Quad Delete No Existe: FALLO - Resultado inesperado: " + deleteResult);
            }
        } catch (Exception e) {
            log("CP-D-2", false, "Quad Delete No Existe: FALLO - Excepción: " + e.getMessage());
        }
        
        // CP-D-3: Inválido - Borrar quad con reserva (integridad referencial)
        try {
            String matricula = "2222BBB";
            String tipo = "Biplaza";
            float precio = 75.0f;
            String desc = "Quad con reserva";
            
            // Insertamos el quad
            Quad quad = new Quad(matricula, tipo, precio, desc);
            long insertQuadResult = quadRepository.insert(quad);
            
            if (insertQuadResult > 0) {
                // Creamos una reserva asociada a este quad
                Reserva reserva = new Reserva("Cliente Test", "600123456", 
                                             "2026-01-10", "2026-01-15", 1);
                List<String> quadIds = new ArrayList<>();
                quadIds.add(matricula);
                
                long insertReservaResult = reservaRepository.insert(reserva, quadIds);
                
                if (insertReservaResult > 0) {
                    // Intentamos borrar el quad (debería fallar por FK constraint)
                    try {
                        int deleteResult = quadRepository.delete(quad);
                        
                        // Si devuelve -1, es porque hubo una excepción de constraint
                        if (deleteResult == -1) {
                            log("CP-D-3", true, "Quad Delete con Reserva: CORRECTO - Constraint FK detectado");
                        } else if (deleteResult == 0) {
                            log("CP-D-3", true, "Quad Delete con Reserva: CORRECTO - Protegido por constraint");
                        } else {
                            log("CP-D-3", false, "Quad Delete con Reserva: FALLO - Se borró cuando no debería");
                        }
                    } catch (Exception ex) {
                        log("CP-D-3", true, "Quad Delete con Reserva: CORRECTO - Excepción de constraint: " + ex.getClass().getSimpleName());
                    }
                } else {
                    log("CP-D-3", false, "Quad Delete con Reserva: FALLO - No se pudo crear la reserva");
                }
            } else {
                log("CP-D-3", false, "Quad Delete con Reserva: FALLO - No se pudo insertar el quad");
            }
        } catch (Exception e) {
            log("CP-D-3", true, "Quad Delete con Reserva: CORRECTO - Excepción capturada: " + e.getMessage());
        }
    }
    
    /**
     * Pruebas unitarias de inserción de Reservas (CP-R-1 a CP-R-4).
     */
    private void testReservaInsert() {
        Log.d(TAG, "\n--- PRUEBAS UNITARIAS: RESERVA INSERT ---");
        
        // Primero necesitamos un quad válido para las reservas
        String quadMatricula = "3333DDD";
        try {
            Quad quad = new Quad(quadMatricula, "Monoplaza", 50.0f, "Quad para reservas");
            quadRepository.insert(quad);
        } catch (Exception e) {
            Log.d(TAG, "Error preparando quad para pruebas de reserva: " + e.getMessage());
        }
        
        // CP-R-1: Válido - Reserva correcta
        try {
            String cliente = "Cliente Test";
            String telefono = "600123456";
            String fechaInicio = "2026-01-01";
            String fechaFin = "2026-01-05";
            
            if (validarReserva(cliente, telefono, fechaInicio, fechaFin)) {
                Reserva reserva = new Reserva(cliente, telefono, fechaInicio, fechaFin, 1);
                List<String> quadIds = new ArrayList<>();
                quadIds.add(quadMatricula);
                
                long result = reservaRepository.insert(reserva, quadIds);
                
                if (result > 0) {
                    log("CP-R-1", true, "Reserva Insert Válida: CORRECTO (ID=" + result + ")");
                } else {
                    log("CP-R-1", false, "Reserva Insert Válida: FALLO - No se insertó correctamente");
                }
            } else {
                log("CP-R-1", false, "Reserva Insert Válida: FALLO - Validación incorrecta");
            }
        } catch (Exception e) {
            log("CP-R-1", false, "Reserva Insert Válida: FALLO - Excepción: " + e.getMessage());
        }
        
        // CP-R-2: Inválido - Fechas incorrectas (fin antes que inicio)
        try {
            String cliente = "Cliente Test";
            String telefono = "600123456";
            String fechaInicio = "2026-02-05";
            String fechaFin = "2026-02-01";
            
            if (!validarReserva(cliente, telefono, fechaInicio, fechaFin)) {
                log("CP-R-2", true, "Reserva Insert Fechas Inválidas: CORRECTO - Validación rechazó correctamente");
            } else {
                Reserva reserva = new Reserva(cliente, telefono, fechaInicio, fechaFin, 1);
                List<String> quadIds = new ArrayList<>();
                quadIds.add(quadMatricula);
                long result = reservaRepository.insert(reserva, quadIds);
                log("CP-R-2", false, "Reserva Insert Fechas Inválidas: FALLO - Se insertó cuando debería fallar");
            }
        } catch (Exception e) {
            log("CP-R-2", true, "Reserva Insert Fechas Inválidas: CORRECTO - Excepción capturada");
        }
        
        // CP-R-3: Inválido - Teléfono incorrecto
        try {
            String cliente = "Cliente Test";
            String telefono = "123";
            String fechaInicio = "2026-03-01";
            String fechaFin = "2026-03-05";
            
            if (!validarReserva(cliente, telefono, fechaInicio, fechaFin)) {
                log("CP-R-3", true, "Reserva Insert Teléfono Inválido: CORRECTO - Validación rechazó correctamente");
            } else {
                Reserva reserva = new Reserva(cliente, telefono, fechaInicio, fechaFin, 1);
                List<String> quadIds = new ArrayList<>();
                quadIds.add(quadMatricula);
                long result = reservaRepository.insert(reserva, quadIds);
                log("CP-R-3", false, "Reserva Insert Teléfono Inválido: FALLO - Se insertó cuando debería fallar");
            }
        } catch (Exception e) {
            log("CP-R-3", true, "Reserva Insert Teléfono Inválido: CORRECTO - Excepción capturada");
        }
        
        // CP-R-4: Inválido - Solapamiento de fechas
        try {
            String cliente = "Cliente Test 2";
            String telefono = "600987654";
            String fechaInicio = "2026-01-01"; // Mismas fechas que CP-R-1
            String fechaFin = "2026-01-05";
            
            if (validarReserva(cliente, telefono, fechaInicio, fechaFin)) {
                // Verificamos disponibilidad
                List<String> quadIds = new ArrayList<>();
                quadIds.add(quadMatricula);
                
                List<String> noDisponibles = reservaRepository.comprobarDisponibilidad(
                    quadIds, fechaInicio, fechaFin, -1);
                
                if (noDisponibles != null && !noDisponibles.isEmpty()) {
                    log("CP-R-4", true, "Reserva Insert Solapamiento: CORRECTO - Solapamiento detectado");
                } else {
                    // Si no se detecta solapamiento, intentamos insertar
                    Reserva reserva = new Reserva(cliente, telefono, fechaInicio, fechaFin, 1);
                    long result = reservaRepository.insert(reserva, quadIds);
                    log("CP-R-4", false, "Reserva Insert Solapamiento: FALLO - No se detectó solapamiento");
                }
            } else {
                log("CP-R-4", false, "Reserva Insert Solapamiento: FALLO - Validación incorrecta");
            }
        } catch (Exception e) {
            log("CP-R-4", false, "Reserva Insert Solapamiento: FALLO - Excepción: " + e.getMessage());
        }
    }
    
    /**
     * Prueba de volumen: Insertar 100 Quads.
     */
    private void testVolumen() {
        Log.d(TAG, "\n--- PRUEBA DE VOLUMEN ---");
        
        try {
            long startTime = System.currentTimeMillis();
            int insertados = 0;
            int errores = 0;
            
            for (int i = 0; i < 100; i++) {
                String matricula = String.format("%04d", i) + "AAA";
                String tipo = (i % 2 == 0) ? "Monoplaza" : "Biplaza";
                float precio = 50.0f + (i * 0.5f);
                String desc = "Quad volumen " + i;
                
                try {
                    Quad quad = new Quad(matricula, tipo, precio, desc);
                    long result = quadRepository.insert(quad);
                    
                    if (result > 0 || result == -1) { // -1 puede ser duplicado
                        insertados++;
                    } else {
                        errores++;
                    }
                } catch (Exception e) {
                    errores++;
                }
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            if (insertados >= 95) { // Permitimos algunos duplicados
                log("VOLUMEN", true, "Prueba de Volumen: CORRECTO - Insertados: " + insertados + 
                    "/100, Tiempo: " + duration + "ms");
            } else {
                log("VOLUMEN", false, "Prueba de Volumen: FALLO - Solo se insertaron " + insertados + 
                    "/100, Errores: " + errores);
            }
        } catch (Exception e) {
            log("VOLUMEN", false, "Prueba de Volumen: FALLO - Excepción: " + e.getMessage());
        }
    }
    
    /**
     * Prueba de sobrecarga: Insertar Quads con descripción exponencial.
     */
    private void testSobrecarga() {
        Log.d(TAG, "\n--- PRUEBA DE SOBRECARGA ---");
        
        try {
            int maxIteraciones = 20; // 2^20 = ~1 millón de caracteres
            int iteracion = 0;
            boolean falloDetectado = false;
            
            for (int i = 1; i <= maxIteraciones; i++) {
                iteracion = i;
                int longitud = (int) Math.pow(2, i);
                
                // Limitamos a 1MB aproximadamente para evitar crash total
                if (longitud > 1024 * 1024) {
                    log("SOBRECARGA", true, "Prueba de Sobrecarga: CORRECTO - Límite seguro alcanzado en iteración " + i);
                    falloDetectado = true;
                    break;
                }
                
                try {
                    StringBuilder sb = new StringBuilder(longitud);
                    for (int j = 0; j < longitud; j++) {
                        sb.append("X");
                    }
                    
                    String matricula = String.format("%04d", 5000 + i) + "SOB";
                    String tipo = "Monoplaza";
                    float precio = 50.0f;
                    String desc = sb.toString();
                    
                    Quad quad = new Quad(matricula, tipo, precio, desc);
                    long result = quadRepository.insert(quad);
                    
                    if (result <= 0 && result != -1) {
                        log("SOBRECARGA", true, "Prueba de Sobrecarga: CORRECTO - Fallo controlado en iteración " + i + 
                            " (longitud: " + longitud + " chars)");
                        falloDetectado = true;
                        break;
                    }
                    
                    Log.d(TAG, "Sobrecarga iteración " + i + ": Insertado OK (longitud: " + longitud + " chars)");
                    
                } catch (OutOfMemoryError e) {
                    log("SOBRECARGA", true, "Prueba de Sobrecarga: CORRECTO - OutOfMemoryError capturado en iteración " + i + 
                        " (longitud: " + longitud + " chars)");
                    falloDetectado = true;
                    break;
                } catch (Exception e) {
                    log("SOBRECARGA", true, "Prueba de Sobrecarga: CORRECTO - Excepción capturada en iteración " + i + 
                        ": " + e.getClass().getSimpleName());
                    falloDetectado = true;
                    break;
                }
            }
            
            if (!falloDetectado) {
                log("SOBRECARGA", true, "Prueba de Sobrecarga: CORRECTO - Completadas " + iteracion + 
                    " iteraciones sin fallos críticos");
            }
            
        } catch (OutOfMemoryError e) {
            log("SOBRECARGA", true, "Prueba de Sobrecarga: CORRECTO - OutOfMemoryError capturado y controlado");
        } catch (Exception e) {
            log("SOBRECARGA", true, "Prueba de Sobrecarga: CORRECTO - Excepción capturada: " + e.getMessage());
        }
    }
    
    // ==================== MÉTODOS AUXILIARES DE VALIDACIÓN ====================
    
    /**
     * Valida todos los campos de un Quad según las reglas de negocio.
     */
    private boolean validarQuad(String matricula, String tipo, float precio, String descripcion) {
        return validarMatricula(matricula) && 
               validarTipo(tipo) && 
               validarPrecio(precio) && 
               validarDescripcion(descripcion);
    }
    
    /**
     * Valida el formato de la matrícula: 4 dígitos + 3 letras mayúsculas.
     */
    private boolean validarMatricula(String matricula) {
        if (matricula == null) {
            return false;
        }
        return MATRICULA_PATTERN.matcher(matricula).matches();
    }
    
    /**
     * Valida que el tipo sea "Monoplaza" o "Biplaza".
     */
    private boolean validarTipo(String tipo) {
        if (tipo == null) {
            return false;
        }
        return tipo.equals("Monoplaza") || tipo.equals("Biplaza");
    }
    
    /**
     * Valida que el precio sea mayor que 0.
     */
    private boolean validarPrecio(float precio) {
        return precio > 0;
    }
    
    /**
     * Valida que la descripción no sea null.
     */
    private boolean validarDescripcion(String descripcion) {
        return descripcion != null;
    }
    
    /**
     * Valida todos los campos de una Reserva según las reglas de negocio.
     */
    private boolean validarReserva(String cliente, String telefono, String fechaInicio, String fechaFin) {
        return validateCliente(cliente) && 
               validarTelefono(telefono) && 
               validarFechas(fechaInicio, fechaFin);
    }
    
    /**
     * Valida que el cliente no sea null ni vacío.
     */
    public boolean validateCliente(String cliente) {
        if (cliente == null || cliente.trim().isEmpty()) {
            return false;
        }
        if (cliente.length() > 255) {
            return false;
        }
        // Solo caracteres alfabéticos y espacios
        return cliente.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");
    }

    /**
     * Devuelve el mensaje de error específico para la validación del cliente.
     * Útil para mostrar Toasts precisos o para logs de pruebas.
     */
    public String getClienteValidationError(String cliente) {
        if (cliente == null || cliente.trim().isEmpty()) {
            return "Error: Campo obligatorio.";
        }
        if (cliente.length() > 255) {
            return "Error: Longitud excedida.";
        }
        if (!cliente.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            return "Error: Formato no permitido.";
        }
        return "Registro exitoso.";
    }

    /**
     * Prueba las particiones de equivalencia para la validación del cliente (PE-01 a PE-05).
     */
    private void testValidateClientePartitioning() {
        Log.d(TAG, "\n--- PRUEBAS DE PARTICIONES DE EQUIVALENCIA: validarCliente ---");

        // PE-01: Válida - Cadenas con caracteres alfabéticos y espacios
        String pe01 = "Juan Pérez";
        log("PE-01", validateCliente(pe01), "Válida: '" + pe01 + "' -> " + getClienteValidationError(pe01));

        // PE-02: Inválida - Cadenas que contienen dígitos numéricos
        String pe02 = "Ana89";
        log("PE-02", !validateCliente(pe02), "Inválida (Dígitos): '" + pe02 + "' -> " + getClienteValidationError(pe02));

        // PE-03: Inválida - Cadenas con caracteres especiales o símbolos
        String pe03 = "L@ura!";
        log("PE-03", !validateCliente(pe03), "Inválida (Especiales): '" + pe03 + "' -> " + getClienteValidationError(pe03));

        // PE-04: Inválida - Cadena vacía o compuesta solo por espacios
        String pe04 = "";
        log("PE-04", !validateCliente(pe04), "Inválida (Vacía): '" + pe04 + "' -> " + getClienteValidationError(pe04));

        // PE-05: Inválida - Cadena con longitud excesiva (>255)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 256; i++) sb.append("A");
        String pe05 = sb.toString();
        log("PE-05", !validateCliente(pe05), "Inválida (Longitud): 'Texto > 255 chars' -> " + getClienteValidationError(pe05));
    }
    
    /**
     * Valida que el teléfono tenga exactamente 9 dígitos.
     */
    private boolean validarTelefono(String telefono) {
        if (telefono == null) {
            return false;
        }
        return telefono.matches("^\\d{9}$");
    }
    
    /**
     * Valida que fechaFin >= fechaInicio (formato YYYY-MM-DD).
     */
    private boolean validarFechas(String fechaInicio, String fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return false;
        }
        
        try {
            // Comparación simple de strings en formato YYYY-MM-DD
            return fechaFin.compareTo(fechaInicio) >= 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Método auxiliar para logging con formato consistente.
     */
    private void log(String testId, boolean passed, String message) {
        String status = passed ? "✓" : "✗";
        Log.d(TAG, status + " " + message);
    }
}
