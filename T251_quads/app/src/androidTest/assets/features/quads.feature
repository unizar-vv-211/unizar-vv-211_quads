Feature: Crear un Quad
  Ejecutar la creacion de un vehiculo Quad en el sistema

  Scenario Outline: Seleccionar anadir quad, introducir matricula, precio, descripcion y confirmar
    Given Estoy en la pantalla principal de Quads
    When Hago clic en crear un quad
    And Introduzco "<matricula>" como matricula
    And Introduzco "<precio>" como precio
    And Introduzco "<descripcion>" como descripcion
    And Confirmo la creacion
    Then Deberia ver "<resultado>" en la lista

    Examples:
      | matricula | precio | descripcion    | resultado |
      | ABC1234   | 50.5   | Quad de prueba | ABC1234   |
      | XYZ5678   | 80.0   | Quad rapido    | XYZ5678   |