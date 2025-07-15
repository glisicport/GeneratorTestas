# Test Generator i Question Manager
Video:
[![Test Generator Demo](https://img.youtube.com/vi/fQ-A1d74_sg/0.jpg)](https://www.youtube.com/watch?v=fQ-A1d74_sg)

Ovaj projekat sadrži dva Java GUI programa razvijena uz pomoć **Maven** alata:

1. **TestGenerator** - GUI aplikacija za generisanje testova.
2. **QuestionManager** - GUI aplikacija za upravljanje pitanjima.

## Izgradnja i Pokretanje Aplikacija

Da biste izgradili i pokrenuli aplikacije, pratite sledeće komande:

```bash
# 1. Izgradnja  aplikacije
mvn clean package

# Pokretanje TestGenerator aplikacije
java -jar target/test-generator.jar

# Pokretanje QuestionManager aplikacije
java -jar target/question-manager.jar

