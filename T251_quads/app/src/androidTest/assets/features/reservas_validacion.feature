Feature: Validacion de reservas
  Como propietario
  Quiero que la app rechace reservas con datos invalidos
  Para evitar registros inconsistentes

  # CPA-R-04, CPA-R-06, CPA-R-09, CPA-R-10, CPA-R-11, CPA-R-13
  Scenario Outline: Rechazar reserva con datos invalidos
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Estoy en la pantalla principal de Reservas
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
      | Raul   | 1         | 10-05-2026 | 11-05-2026   | 1      | AAA1111 |
      | Raul   | 123456789 | 33-05-2026 | 11-05-2026   | 1      | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 |              | 1      | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 | 09-05-2026   | 1      | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 | 11-05-2026   | 15     | AAA1111 |
      | Raul   | 123456789 | 10-05-2026 | 11-05-2026   | 1      |         |
