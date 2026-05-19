Feature: Precio pactado de reservas
  Como propietario
  Quiero conservar el precio total pactado al crear la reserva
  Para que cambios posteriores en el inventario no alteren reservas existentes

  # CPA-P-01
  Scenario: Mantener el precio de la reserva al modificar el precio del quad en el inventario
    Given Creo desde la interfaz un quad "AAA1111" de tipo "Monoplaza" con precio "50.0" y descripcion "Quad con precio inicial"
    And Creo desde la interfaz una reserva para "Raul" con telefono "123456789" del "10-05-2026" al "11-05-2026", "1" casco y quad "AAA1111"
    And Vuelvo a la pantalla principal
    When Modifico el precio base del quad "AAA1111" en el inventario a "200.0" euros
    And Navego al listado de reservas
    Then La reserva de "Raul" debe seguir mostrando un precio total de "50.0"
