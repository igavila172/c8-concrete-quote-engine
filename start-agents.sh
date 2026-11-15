#!/bin/bash

# start-agents.sh - C-8 Concrete Quote Engine Command Center

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Initializing C-8 Concrete Command Center...${NC}"

# Check for dependencies
check_dep() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}Error: $1 is not installed.${NC}"
        echo -e "Please run: ${GREEN}brew install $2${NC}"
        return 1
    fi
    return 0
}

check_dep tmux tmux || exit 1
check_dep btop btop || echo -e "${YELLOW}Warning: btop not found. System monitor will use top instead.${NC}"

SESSION="c8_command_center"

# Kill existing session if it exists
tmux kill-session -t $SESSION 2>/dev/null

# Create new session and detach
tmux new-session -d -s $SESSION -n "Agents"

# Pane 0: Backend Agent (Maven Boot)
tmux send-keys -t $SESSION.0 "./mvnw spring-boot:run" C-m

# Split vertically for Frontend/Monitor
tmux split-window -h -t $SESSION

# Pane 1: Frontend Agent (Asset watcher or just a placeholder for now)
tmux send-keys -t $SESSION.1 "echo 'Frontend Agent active. Monitoring static/index.html...'; ls -R src/main/resources/static/ | grep 'html\|js\|css'" C-m

# Split Pane 1 horizontally for System Monitor
tmux split-window -v -t $SESSION.1

# Pane 2: System Monitor (btop or top)
if command -v btop &> /dev/null; then
    tmux send-keys -t $SESSION.2 "btop" C-m
else
    tmux send-keys -t $SESSION.2 "top" C-m
fi

# Select Pane 0 as focus
tmux select-pane -t $SESSION.0

# Attach to session
echo -e "${GREEN}Command Center started! Attaching to tmux session: $SESSION${NC}"
tmux attach-session -t $SESSION
