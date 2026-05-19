Feature: Modificacion de quads
  Como propietario
  Quiero modificar los datos de un quad
  Para mantener actualizado el inventario

  # CPA-MQ-01
  Scenario: Modificar quad existente con datos validos
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Estoy en la pantalla principal de Quads
    When Selecciono el quad "AAA1111"
    And Elijo editar el quad
    And Actualizo el precio a "25.0" y la descripcion a "Quad actualizado"
    And Confirmo la creacion
    Then El quad "AAA1111" debe tener precio "25.0" y descripcion "Quad actualizado"

  # CPA-MQ-03
  Scenario: Rechazar modificacion de quad con formato invalido
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Estoy en la pantalla principal de Quads
    When Selecciono el quad "AAA1111"
    And Elijo editar el quad
    And Actualizo el precio a "-10.0" y la descripcion a "Precio invalido"
    And Confirmo la creacion
    Then El sistema debe mantenerme en la pantalla de creacion de Quads
