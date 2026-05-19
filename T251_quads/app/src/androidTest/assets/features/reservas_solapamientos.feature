Feature: Solapamientos de reservas
  Como propietario
  Quiero que el sistema compruebe la disponibilidad del quad
  Para no asignar el mismo quad en intervalos incompatibles

  # CPA-S-01
  Scenario: Crear reserva sobre quad libre
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Estoy en la pantalla principal de Reservas
    When Hago clic en crear una reserva
    And Introduzco "Raul" como cliente
    And Introduzco "123456789" como telefono
    And Introduzco "10-05-2026" como fecha de recogida
    And Introduzco "15-05-2026" como fecha de devolucion
    And Introduzco "1" como numero de cascos
    And Selecciono los quads "AAA1111"
    And Confirmo la creacion de la reserva
    Then Deberia ver "Raul" en la lista de reservas

  # CPA-S-02, CPA-S-03, CPA-S-04
  Scenario Outline: Rechazar reserva solapada sobre el mismo quad
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Existe una reserva previa para "Raul" con el quad "AAA1111" del "10-05-2026" al "15-05-2026"
    And Estoy en la pantalla principal de Reservas
    When Hago clic en crear una reserva
    And Introduzco "Raul" como cliente
    And Introduzco "123456789" como telefono
    And Introduzco "<f_recogida>" como fecha de recogida
    And Introduzco "<f_devolucion>" como fecha de devolucion
    And Introduzco "1" como numero de cascos
    And Selecciono los quads "AAA1111"
    And Confirmo la creacion de la reserva
    Then El sistema debe mantenerme en la pantalla de creacion de Reservas

    Examples:
      | f_recogida | f_devolucion |
      | 10-05-2026 | 15-05-2026   |
      | 08-05-2026 | 12-05-2026   |
      | 13-05-2026 | 18-05-2026   |
