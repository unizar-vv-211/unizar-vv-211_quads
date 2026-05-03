Feature: Gestion de Reservas y Mantenimiento de Precio (RF-2 y RF-3)
  Como empleado de la tienda
  Quiero gestionar las reservas de los clientes y asegurar sus tarifas
  Para alquilar los quads de forma segura sin solapamientos ni perdidas economicas

  # SCENARIO TESTING (Flujo de Negocio Nominal)
  Scenario: Registro exitoso de una reserva valida
    Given Existe un quad "RES1111" disponible con precio "50.0"
    And Estoy en la pantalla de creacion de Reservas
    When Introduzco los datos del cliente "Juan Perez" con telefono "600111222"
    And Solicito "0" cascos
    And Selecciono fechas del "10-05-2026" al "12-05-2026"
    And Asigno el quad "RES1111"
    And Confirmo la reserva
    Then Deberia ver la reserva de "Juan Perez" en el listado principal

  # PARTICIONES DE EQUIVALENCIA (Prevencion de Errores Comerciales y Solapamientos)
  Scenario Outline: Rechazo de reservas con datos invalidos o conflictos de calendario
    Given Existe un quad "RES1111" disponible
    And Existe una reserva previa del quad "RES1111" entre "15-05-2026" y "20-05-2026"
    And Estoy en la pantalla de creacion de Reservas
    When Introduzco los datos del cliente "<cliente>" con telefono "<telefono>"
    And Solicito "<cascos>" cascos
    And Selecciono fechas del "<inicio>" al "<fin>"
    And Asigno el quad "RES1111"
    And Confirmo la reserva
    Then El sistema debe mantenerme en la pantalla de creacion de Reservas

    Examples:
      | caso_de_negocio                       | cliente    | telefono  | cascos | inicio     | fin        |
      | Falta numero de contacto              | Ana Lopez  |           | 1      | 01-06-2026 | 05-06-2026 |
      | Inconsistencia temporal (Invertidas)  | Luis Gomez | 600333444 | 1      | 10-06-2026 | 05-06-2026 |
      | Fecha inexistente en el calendario    | Eva Ruiz   | 600555666 | 1      | 32-05-2026 | 05-06-2026 |
      | Conflicto de doble alquiler (Solape)  | Alex Pol   | 600777888 | 1      | 16-05-2026 | 18-05-2026 |
      | Exceso de cascos para el tipo de quad | Sara Paz   | 600999000 | 5      | 01-07-2026 | 05-07-2026 |

  # MANTENIMIENTO DEL PRECIO (Price Freeze)
  Scenario: Congelacion de tarifa en reservas historicas
    Given Una reserva activa para "Ana Lopez" vinculada al quad "RES1111" con un precio total pactado de "100.0"
    When Modifico el precio base del quad "RES1111" en el inventario a "90.0" euros
    And Navego al listado de reservas
    Then La reserva de "Ana Lopez" debe seguir mostrando un precio total de "100.0"