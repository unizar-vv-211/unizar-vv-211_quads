Feature: Gestion de Reservas
  Como propietario
  Quiero registrar las reservas de los clientes
  Para gestionar la disponibilidad de quads

  Background:
    Given Existe un quad con matricula "AAA1111" y precio "50.0"

  # SCENARIO TESTING - Particiones de Equivalencia Válidas (Prueba 1)
  Scenario Outline: Crear una reserva valida
    Given Estoy en la pantalla principal de Reservas
    When Hago clic en crear una reserva
    And Introduzco "<nombre>" como cliente
    And Introduzco "<telefono>" como telefono
    And Introduzco "<f_recogida>" como fecha de recogida
    And Introduzco "<f_devolucion>" como fecha de devolucion
    And Introduzco "<cascos>" como numero de cascos
    And Selecciono los quads "<quad>"
    And Confirmo la creacion de la reserva
    Then Deberia ver "<nombre>" en la lista de reservas

    Examples:
      | nombre      | telefono  | f_recogida | f_devolucion | cascos | quad    |
      | Raul        | 123456789 | 10-05-2026 | 11-05-2026   | 1      | AAA1111 |

  # SCENARIO TESTING - Particiones de Equivalencia Inválidas (Pruebas 2 a 12)
  Scenario Outline: Rechazo de reservas con datos invalidos
    Given Estoy en la pantalla principal de Reservas
    When Hago clic en crear una reserva
    And Introduzco "<nombre>" como cliente
    And Introduzco "<telefono>" como telefono
    And Introduzco "<f_recogida>" como fecha de recogida
    And Introduzco "<f_devolucion>" como fecha de devolucion
    And Introduzco "<cascos>" como numero de cascos
    And Selecciono los quads "<quad>"
    And Confirmo la creacion de la reserva
    Then El sistema debe mantenerme en la pantalla de creacion de Reservas

    Examples:
      | nombre | telefono  | f_recogida | f_devolucion | cascos | quad    |
      |        | 123456789 | 10-05-2026 | 11-05-2026   | 1      | AAA1111 |
      | Raul   | 1         | 10-05-2026 | 11-05-2026   | 1      | AAA1111 |
      | Raul   |           | 10-05-2026 | 11-05-2026   | 1      | AAA1111 |
      | Raul   | 123456789 | 33-05-2026 | 11-05-2026   | 1      | AAA1111 |
      | Raul   | 123456789 |            | 11-05-2026   | 1      | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 | 33-05-2026   | 1      | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 |              | 1      | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 | 09-05-2026   | 1      | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 | 11-05-2026   | 15     | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 | 11-05-2026   |        | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 | 11-05-2026   | 1      |         |

# SCENARIO TESTING - Regla de Negocio: Congelamiento de Precios
  Scenario: Mantener el precio de la reserva al modificar el precio del quad en el inventario
    Given Una reserva activa para "Raul" vinculada al quad "AAA1111" con un precio total pactado de "50.0"
    When Modifico el precio base del quad "AAA1111" en el inventario a "200.0" euros
    And Navego al listado de reservas
    Then La reserva de "Raul" debe seguir mostrando un precio total de "50.0"