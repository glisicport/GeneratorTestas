# Test Generator i Question Manager

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
