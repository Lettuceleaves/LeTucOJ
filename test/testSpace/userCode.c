#include <stdio.h>

int main(void) {
    int a, b, c;
    scanf("%d%d%d", &a, &b, &c);
    int nums[3];
    nums[3] = a;
    printf("%d", nums[3] + b + c); 
}