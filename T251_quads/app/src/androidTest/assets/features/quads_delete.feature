Feature: Eliminacion de quads
  Como propietario
  Quiero eliminar quads del inventario
  Para retirar vehiculos que ya no estan disponibles

  # CPA-DQ-01
  Scenario: Eliminar quad existente
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Estoy en la pantalla principal de Quads
    When Selecciono el quad "AAA1111"
    And Elijo eliminar el quad
    Then No deberia ver "AAA1111" en la lista de quads
