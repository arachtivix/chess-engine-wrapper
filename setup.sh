#!/bin/bash

# Setup script for chess-engine-wrapper development environment
# Installs Clojure CLI tools and Stockfish chess engine

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Clojure CLI version (matching CI configuration)
CLOJURE_VERSION="1.11.1.1429"

echo "=========================================="
echo "Chess Engine Wrapper - Development Setup"
echo "=========================================="
echo ""

# Detect OS
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
    echo -e "${GREEN}Detected OS: Linux${NC}"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
    echo -e "${GREEN}Detected OS: macOS${NC}"
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
    OS="windows"
    echo -e "${YELLOW}Detected OS: Windows${NC}"
    echo -e "${YELLOW}Note: This script may have limited functionality on Windows.${NC}"
    echo -e "${YELLOW}Consider using WSL (Windows Subsystem for Linux) for better compatibility.${NC}"
else
    echo -e "${RED}Unknown OS: $OSTYPE${NC}"
    echo "Please install Clojure and Stockfish manually."
    echo "See README.md for installation instructions."
    exit 1
fi

echo ""

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Install Clojure CLI tools
install_clojure() {
    echo "Installing Clojure CLI tools (version $CLOJURE_VERSION)..."
    
    if [[ "$OS" == "linux" ]]; then
        # Linux installation
        curl -L -O "https://github.com/clojure/brew-install/releases/download/$CLOJURE_VERSION/linux-install.sh"
        chmod +x linux-install.sh
        sudo ./linux-install.sh
        rm linux-install.sh
        echo -e "${GREEN}✓ Clojure CLI installed${NC}"
        
    elif [[ "$OS" == "macos" ]]; then
        # macOS installation
        if command_exists brew; then
            brew install clojure/tools/clojure
            echo -e "${GREEN}✓ Clojure CLI installed via Homebrew${NC}"
        else
            echo -e "${YELLOW}Homebrew not found. Installing Clojure manually...${NC}"
            curl -L -O "https://github.com/clojure/brew-install/releases/download/$CLOJURE_VERSION/posix-install.sh"
            chmod +x posix-install.sh
            sudo ./posix-install.sh
            rm posix-install.sh
            echo -e "${GREEN}✓ Clojure CLI installed${NC}"
        fi
        
    elif [[ "$OS" == "windows" ]]; then
        echo -e "${YELLOW}For Windows, please install Clojure manually:${NC}"
        echo "1. Download and run the installer from:"
        echo "   https://github.com/clojure/tools.deps.alpha/wiki/clj-on-Windows"
        echo "2. Or use Scoop: scoop install clojure"
        echo ""
        return 1
    fi
}

# Install Stockfish
install_stockfish() {
    echo "Installing Stockfish chess engine..."
    
    if [[ "$OS" == "linux" ]]; then
        # Linux installation (Ubuntu/Debian)
        sudo apt-get update
        sudo apt-get install -y stockfish
        echo -e "${GREEN}✓ Stockfish installed${NC}"
        
    elif [[ "$OS" == "macos" ]]; then
        # macOS installation
        if command_exists brew; then
            brew install stockfish
            echo -e "${GREEN}✓ Stockfish installed via Homebrew${NC}"
        else
            echo -e "${RED}Error: Homebrew is required to install Stockfish on macOS${NC}"
            echo "Please install Homebrew first: https://brew.sh"
            return 1
        fi
        
    elif [[ "$OS" == "windows" ]]; then
        echo -e "${YELLOW}For Windows, please install Stockfish manually:${NC}"
        echo "1. Download from: https://stockfishchess.org/download/"
        echo "2. Extract and add to your PATH"
        echo ""
        return 1
    fi
}

# Check existing installations
echo "Checking existing installations..."
echo ""

CLOJURE_INSTALLED=false
STOCKFISH_INSTALLED=false

if command_exists clojure; then
    CLOJURE_VERSION_INSTALLED=$(clojure --version 2>&1 | head -1 || echo "unknown")
    echo -e "${GREEN}✓ Clojure is already installed: $CLOJURE_VERSION_INSTALLED${NC}"
    CLOJURE_INSTALLED=true
else
    echo -e "${YELLOW}✗ Clojure is not installed${NC}"
fi

if command_exists stockfish; then
    STOCKFISH_VERSION_INSTALLED=$(stockfish --version 2>&1 | head -1 || echo "unknown")
    echo -e "${GREEN}✓ Stockfish is already installed: $STOCKFISH_VERSION_INSTALLED${NC}"
    STOCKFISH_INSTALLED=true
else
    echo -e "${YELLOW}✗ Stockfish is not installed${NC}"
fi

echo ""

# Install missing components
if [[ "$CLOJURE_INSTALLED" == false ]]; then
    install_clojure
    echo ""
fi

if [[ "$STOCKFISH_INSTALLED" == false ]]; then
    install_stockfish
    echo ""
fi

# Verify installations
echo "Verifying installations..."
echo ""

if command_exists clojure; then
    echo -e "${GREEN}✓ Clojure CLI is available${NC}"
    clojure --version
else
    echo -e "${RED}✗ Clojure CLI installation failed or is not in PATH${NC}"
fi

echo ""

if command_exists stockfish; then
    echo -e "${GREEN}✓ Stockfish is available${NC}"
    echo "  Location: $(which stockfish)"
else
    echo -e "${RED}✗ Stockfish installation failed or is not in PATH${NC}"
fi

echo ""
echo "=========================================="
echo "Setup complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Run tests:          clojure -M:test"
echo "2. Build JAR:          clojure -T:build jar"
echo "3. Check version:      clojure -T:build get-version"
echo "4. Run examples:       clojure -M -m example-display"
echo ""
