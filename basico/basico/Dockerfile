FROM maven:3.9-amazoncorretto

# LEvantarlo como root únicamente para verificar el problema
# Crear usuario nonroot
RUN groupadd -r nonroot && useradd -r -g nonroot -m nonroot \
    && mkdir -p /basico \
    && chown -R nonroot:nonroot /basico

WORKDIR /basico

# Copiar el código y compilar como root (para evitar problemas de permisos en.m2)
COPY .. .

RUN mvn clean install

RUN chown -R nonroot:nonroot /basico

# Cambiar a usuario sin root para la ejecución
USER nonroot

CMD ["mvn", "spring-boot:run"]

CMD mvn spring-boot:run