#include <stdio.h>
#include <stdlib.h>

int main() {
    int a, b, c;
    scanf("%d%d%d", &a, &b, &c);
    int n[3] = {a, b, c};
    for (int i = 0; i < 3; i++) {
        printf("%d ", n[i]);
    }
    return 0;
}