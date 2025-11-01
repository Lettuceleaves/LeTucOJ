N=$1
LANG=$2

if [ -z "$N" ] || [ -z "$LANG" ]; then
    echo "5" > /submission/status.txt; exit 1
fi
cd /submission || { echo "5" > status.txt; exit 1; }

CONFIG_FILE="config.yaml"
if [ ! -f "$CONFIG_FILE" ]; then
    echo "5" > status.txt; exit 1
fi

> err.txt; > compile.txt; > status.txt

TEMP_TIME_FILE="time.tmp"
> "$TEMP_TIME_FILE"

MAX_MEM_KB=0

if [ $? -ne 0 ]; then
    echo "2" > status.txt; rm -f "$TEMP_TIME_FILE"; exit 0
fi

for i in $(seq 1 $N); do

    if [ ! -f "in_$i.txt" ]; then
        echo "5" > status.txt; rm -f "$TEMP_TIME_FILE"; exit 0
    fi

    idx=$((i - 1))
    WALL_S=$(yq e ".test_cases[$idx].$LANG.time_limit_wall_s // .language_defaults.$LANG.time_limit_wall_s" "$CONFIG_FILE")

    if [ -z "$WALL_S" ]; then
        echo "5" > status.txt; rm -f "$TEMP_TIME_FILE"; exit 1
    fi

    
    

    (timeout "$WALL_S" /usr/bin/time -f "%M" -o "$TEMP_TIME_FILE" \
    python prog.py < "in_$i.txt" >> "ou_$i.txt" 2>> "err.txt")
    exit_code=$?




    if [ $exit_code -eq 124 ]; then
        echo "4" > status.txt; rm -f "$TEMP_TIME_FILE"; exit 0
    elif [ $exit_code -eq 137 ]; then
        echo "3" > status.txt; rm -f "$TEMP_TIME_FILE"; exit 0
    elif [ $exit_code -ne 0 ]; then
        echo "3" > status.txt; rm -f "$TEMP_TIME_FILE"; exit 0
    fi

    CURRENT_MEM_KB=$(awk 'NR==1 {print $1}' "$TEMP_TIME_FILE")
    
    if [ -z "$CURRENT_MEM_KB" ]; then
        echo "5" > status.txt; rm -f "$TEMP_TIME_FILE"; exit 1
    fi

    if [ "$CURRENT_MEM_KB" -gt "$MAX_MEM_KB" ]; then
        MAX_MEM_KB=$CURRENT_MEM_KB
    fi
done

rm -f "$TEMP_TIME_FILE"
echo "0 " > status.txt
echo "$MAX_MEM_KB" > err.txt

exit 0
python prog.py