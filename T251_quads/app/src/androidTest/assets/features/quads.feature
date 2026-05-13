Feature: Gestion del Inventario de Quads
  Como empleado de la tienda de alquiler
  Quiero mantener actualizado el catalogo de quads
  Para poder asignarlos posteriormente a las reservas de los clientes

  # SCENARIO TESTING - Particiones de Equivalencia Válidas (Pruebas 1 y 2)
  Scenario Outline: Crear quad con datos validos
    Given Estoy en la pantalla principal de Quads
    When Hago clic en crear un quad
    And Introduzco "<matricula>" como matricula
    And Selecciono el tipo "<tipo>"
    And Introduzco "<precio>" como precio
    And Introduzco "<descripcion>" como descripcion
    And Confirmo la creacion
    Then Deberia ver "<matricula>" en la lista

    Examples:
      | matricula | tipo      | precio | descripcion                            |
      | AAA1111   | Monoplaza | 10.0   | Quad validacion monoplaza              |
      | AAA1112   | Biplaza   | 10.0   | Quad validacion biplaza                |

  # SCENARIO TESTING - Particiones de Equivalencia Inválidas (Pruebas 3, 4, 5, 6, 7)
  Scenario Outline: Rechazo de quads con datos invalidos
    Given Estoy en la pantalla principal de Quads
    When Hago clic en crear un quad
    And Introduzco "<matricula>" como matricula
    And Selecciono el tipo "<tipo>"
    And Introduzco "<precio>" como precio
    And Introduzco "<descripcion>" como descripcion
    And Confirmo la creacion
    Then El sistema debe mantenerme en la pantalla de creacion de Quads

    Examples:
      | matricula | tipo      | precio | descripcion                     |
      | 1111AAA   | Monoplaza | 10.0   | Prueba formato invalido         |
      |           | Monoplaza | 10.0   | Prueba matricula vacia          |
      | AAA1113   | Monoplaza | -10.0  | Prueba precio negativo          |
      | AAA1114   | Monoplaza |        | Prueba precio vacio             |
      | AAA1115   | Monoplaza | 10.0   |                                 |