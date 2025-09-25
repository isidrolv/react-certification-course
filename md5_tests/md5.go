package md5_tests

import (
	"crypto/md5"
	"fmt"
	"strings"
)

// MD5Hasher estructura para manejar operaciones de hash MD5
type MD5Hasher struct{}

// GenerateHash genera un hash MD5 de la concatenación de múltiples strings
func (h *MD5Hasher) GenerateHash(inputs ...string) string {
	concatenated := strings.Join(inputs, "")
	hash := md5.Sum([]byte(concatenated))
	return fmt.Sprintf("%x", hash)
}

// GenerateShortHash genera un hash MD5 y retorna los primeros n caracteres
func (h *MD5Hasher) GenerateShortHash(length int, inputs ...string) string {
	if length < 0 {
		return ""
	}
	fullHash := h.GenerateHash(inputs...)
	if length > len(fullHash) {
		length = len(fullHash)
	}
	return fullHash[:length]
}

// ResourceNamer estructura para generar nombres de recursos
type ResourceNamer struct {
	hasher *MD5Hasher
}

// NewResourceNamer crea una nueva instancia de ResourceNamer
func NewResourceNamer() *ResourceNamer {
	return &ResourceNamer{
		hasher: &MD5Hasher{},
	}
}

// GenerateResourceSuffix genera un sufijo único para recursos
func (rn *ResourceNamer) GenerateResourceSuffix(vpcName, accountID string) string {
	return rn.hasher.GenerateShortHash(12, vpcName, accountID)
}

// GenerateResourceName genera un nombre completo de recurso
func (rn *ResourceNamer) GenerateResourceName(resourceType, vpcName, accountID string) string {
	suffix := rn.GenerateResourceSuffix(vpcName, accountID)
	return fmt.Sprintf("%s-%s-%s", resourceType, vpcName, suffix)
}

// QuickMD5 función simple para generar MD5 rápidamente
func QuickMD5(vpcName, accountID string, length int) string {
	if length < 0 {
		return ""
	}
	concatenated := vpcName + accountID
	hash := md5.Sum([]byte(concatenated))
	md5Hash := fmt.Sprintf("%x", hash)

	if length > len(md5Hash) {
		length = len(md5Hash)
	}

	return md5Hash[:length]
}

// GenerateAWSResourceNames genera nombres para recursos AWS comunes
func GenerateAWSResourceNames(vpcName, accountID string) map[string]string {
	suffix := QuickMD5(vpcName, accountID, 12)

	return map[string]string{
		"vpc":              fmt.Sprintf("vpc-%s-%s", vpcName, suffix),
		"subnet_public":    fmt.Sprintf("subnet-pub-%s-%s", vpcName, suffix),
		"subnet_private":   fmt.Sprintf("subnet-prv-%s-%s", vpcName, suffix),
		"security_group":   fmt.Sprintf("sg-%s-%s", vpcName, suffix),
		"route_table":      fmt.Sprintf("rt-%s-%s", vpcName, suffix),
		"internet_gateway": fmt.Sprintf("igw-%s-%s", vpcName, suffix),
		"nat_gateway":      fmt.Sprintf("nat-%s-%s", vpcName, suffix),
		"load_balancer":    fmt.Sprintf("alb-%s-%s", vpcName, suffix),
	}
}
