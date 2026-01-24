#!/bin/bash

# Script pour vérifier que tout est prêt avant le déploiement Maven Central

echo "🔍 Vérification de la configuration pour le déploiement Maven Central..."
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Compteurs
ERRORS=0
WARNINGS=0
SUCCESS=0

# Fonction pour afficher les résultats
check_success() {
    echo -e "${GREEN}✅ $1${NC}"
    ((SUCCESS++))
}

check_error() {
    echo -e "${RED}❌ $1${NC}"
    ((ERRORS++))
}

check_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
    ((WARNINGS++))
}

# 1. Vérifier GPG
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1️⃣  Vérification GPG"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v gpg &> /dev/null; then
    check_success "GPG est installé"
    
    # Vérifier les clés
    if gpg --list-secret-keys | grep -q "sec"; then
        check_success "Clé GPG privée trouvée"
        echo "   Clés disponibles:"
        gpg --list-secret-keys --keyid-format LONG | grep -A 1 "sec"
    else
        check_error "Aucune clé GPG privée trouvée. Exécutez: gpg --full-generate-key"
    fi
else
    check_error "GPG n'est pas installé. Exécutez: brew install gnupg"
fi

echo ""

# 2. Vérifier Maven
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2️⃣  Vérification Maven"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    check_success "Maven est installé: $MVN_VERSION"
else
    check_error "Maven n'est pas installé"
fi

echo ""

# 3. Vérifier Java
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3️⃣  Vérification Java"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    check_success "Java est installé: $JAVA_VERSION"
    
    # Vérifier la version Java 17
    if java -version 2>&1 | grep -q "version \"17"; then
        check_success "Java 17 détecté (requis pour le projet)"
    else
        check_warning "Java 17 n'est pas la version active (requis pour le projet)"
    fi
else
    check_error "Java n'est pas installé"
fi

echo ""

# 4. Vérifier le pom.xml
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4️⃣  Vérification du pom.xml"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

cd agent 2>/dev/null || cd ../agent 2>/dev/null

if [ -f "pom.xml" ]; then
    check_success "pom.xml trouvé"
    
    # Vérifier les éléments requis
    if grep -q "<groupId>com.capricedumardi</groupId>" pom.xml; then
        check_success "groupId correct: com.capricedumardi"
    else
        check_error "groupId incorrect dans pom.xml"
    fi
    
    if grep -q "<name>Langa-Agent</name>" pom.xml; then
        check_success "name défini"
    else
        check_warning "name non défini dans pom.xml"
    fi
    
    if grep -q "<description>" pom.xml && ! grep -q "<description>Langa-Agent</description>" pom.xml; then
        check_success "description définie"
    else
        check_warning "description manquante ou générique"
    fi
    
    if grep -q "<url>https://github.com" pom.xml; then
        check_success "URL du projet définie"
    else
        check_warning "URL du projet manquante"
    fi
    
    if grep -q "<license>" pom.xml && ! grep -q "<license/>" pom.xml; then
        check_success "license définie"
    else
        check_error "license manquante (requise pour Maven Central)"
    fi
    
    if grep -q "<developer>" pom.xml && ! grep -q "<developer/>" pom.xml; then
        check_success "developer défini"
    else
        check_error "developer manquant (requis pour Maven Central)"
    fi
    
    if grep -q "<scm>" pom.xml && ! grep -q "<connection/>" pom.xml; then
        check_success "SCM défini"
    else
        check_error "SCM manquant (requis pour Maven Central)"
    fi
    
    if grep -q "maven-source-plugin" pom.xml; then
        check_success "maven-source-plugin configuré"
    else
        check_error "maven-source-plugin manquant"
    fi
    
    if grep -q "maven-javadoc-plugin" pom.xml; then
        check_success "maven-javadoc-plugin configuré"
    else
        check_error "maven-javadoc-plugin manquant"
    fi
    
    if grep -q "maven-gpg-plugin" pom.xml; then
        check_success "maven-gpg-plugin configuré"
    else
        check_error "maven-gpg-plugin manquant"
    fi
    
    if grep -q "central-publishing-maven-plugin" pom.xml; then
        check_success "central-publishing-maven-plugin configuré"
    else
        check_error "central-publishing-maven-plugin manquant"
    fi
    
else
    check_error "pom.xml non trouvé"
fi

echo ""

# 5. Tester la compilation
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5️⃣  Test de compilation"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f "pom.xml" ]; then
    echo "   Compilation en cours..."
    if mvn clean compile -q; then
        check_success "Compilation réussie"
    else
        check_error "Échec de la compilation"
    fi
else
    check_warning "Impossible de tester la compilation (pom.xml non trouvé)"
fi

echo ""

# 6. Vérifier GitHub Actions
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "6️⃣  Vérification GitHub Actions"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

cd ..
if [ -f ".github/workflows/maven-publish.yml" ]; then
    check_success "Workflow maven-publish.yml trouvé"
else
    check_error "Workflow maven-publish.yml manquant"
fi

echo ""

# Résumé
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 RÉSUMÉ"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}✅ Succès: $SUCCESS${NC}"
echo -e "${YELLOW}⚠️  Avertissements: $WARNINGS${NC}"
echo -e "${RED}❌ Erreurs: $ERRORS${NC}"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}🎉 Tout est prêt pour le déploiement !${NC}"
    echo ""
    echo "Prochaines étapes:"
    echo "1. Configurez les secrets GitHub (voir DEPLOYMENT_CHECKLIST.md)"
    echo "2. Créez une release sur GitHub ou déclenchez le workflow manuellement"
    echo "3. Vérifiez le déploiement sur https://s01.oss.sonatype.org/"
    exit 0
else
    echo -e "${RED}⚠️  Corrigez les erreurs avant de déployer${NC}"
    echo ""
    echo "Consultez le guide complet: MAVEN_CENTRAL_DEPLOYMENT_GUIDE.md"
    exit 1
fi
